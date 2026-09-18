package com.zidio.keystone.service;

import com.zidio.keystone.domain.*;
import com.zidio.keystone.domain.enums.Role;
import com.zidio.keystone.domain.enums.WorkOrderStatus;
import com.zidio.keystone.dto.workorder.AssignRequest;
import com.zidio.keystone.dto.workorder.StatusUpdateRequest;
import com.zidio.keystone.dto.workorder.WorkOrderRequest;
import com.zidio.keystone.dto.workorder.WorkOrderResponse;
import com.zidio.keystone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkOrderService {

    private final UserRepository userRepository;
    private final WorkOrderRepository workOrderRepository;
    private final WorkOrderStatusHistoryRepository historyRepository;
    private final CustomerRepository customerRepository;
    private final SiteRepository siteRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional
    public WorkOrderResponse create(WorkOrderRequest request) {
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + request.customerId()));

        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new NoSuchElementException("Site not found: " + request.siteId()));

        if (!site.getCustomer().getId().equals(request.customerId())) {
            throw new IllegalArgumentException("Site " + site.getId() + " does not belong to customer " + customer.getId());
        }

        Instant now = Instant.now();
        WorkOrder workOrder = WorkOrder.builder()
                .code(generateUniqueCode())
                .title(request.title())
                .description(request.description())
                .priority(request.priority())
                .status(WorkOrderStatus.NEW)
                .customer(customer)
                .site(site)
                .slaDueAt(now.plus(request.priority().getSlaHours(), ChronoUnit.HOURS))
                .build();

        WorkOrder saved = workOrderRepository.save(workOrder);

        User currentUser = currentUser();
        historyRepository.save(WorkOrderStatusHistory.builder()
                .workOrder(saved)
                .fromStatus(null)
                .toStatus(WorkOrderStatus.NEW)
                .changedBy(currentUser)
                .note("Work order created")
                .build());

        return WorkOrderResponse.from(saved);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public WorkOrderResponse getById(UUID id) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Work order not found: " + id));

        User currentUser = currentUser();
        boolean allowed = switch (currentUser.getRole()) {
            case DISPATCHER, MANAGER -> true;
            case TECHNICIAN -> wo.getAssignedTo() != null && wo.getAssignedTo().getId().equals(currentUser.getId());
            case CUSTOMER -> wo.getCustomer().getId().equals(currentUser.getCustomer().getId());
        };

        if (!allowed) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to view this work order");
        }

        return WorkOrderResponse.from(wo);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional(readOnly = true)
    public Page<WorkOrderResponse> list(Pageable pageable) {
        User currentUser = currentUser();

        return switch (currentUser.getRole()) {
            case TECHNICIAN -> workOrderRepository.findByAssignedToId(currentUser.getId(), pageable)
                    .map(WorkOrderResponse::from);
            case CUSTOMER -> workOrderRepository.findByCustomerId(currentUser.getCustomer().getId(), pageable)
                    .map(WorkOrderResponse::from);
            case DISPATCHER, MANAGER -> workOrderRepository.findAll(pageable)
                    .map(WorkOrderResponse::from);
        };
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional
    public WorkOrderResponse update(UUID id, WorkOrderRequest request) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Work order not found: " + id));

        if (wo.getStatus().isTerminal()) {
            throw new IllegalStateException("Cannot edit a work order that is " + wo.getStatus());
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NoSuchElementException("Customer not found: " + request.customerId()));
        Site site = siteRepository.findById(request.siteId())
                .orElseThrow(() -> new NoSuchElementException("Site not found: " + request.siteId()));

        if (!site.getCustomer().getId().equals(customer.getId())) {
            throw new IllegalArgumentException("Site " + site.getId() + " does not belong to customer " + customer.getId());
        }

        wo.setCustomer(customer);
        wo.setSite(site);
        wo.setTitle(request.title());
        wo.setDescription(request.description());
        wo.setPriority(request.priority());

        return WorkOrderResponse.from(wo);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional
    public WorkOrderResponse assign(UUID id, AssignRequest request) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Work order not found: " + id));

        if (!wo.getStatus().canTransitionTo(WorkOrderStatus.ASSIGNED)) {
            throw new IllegalStateException("Cannot assign a work order in status " + wo.getStatus());
        }

        User technician = userRepository.findById(request.technicianId())
                .orElseThrow(() -> new NoSuchElementException("User not found: " + request.technicianId()));

        if (technician.getRole() != Role.TECHNICIAN) {
            throw new IllegalArgumentException("User " + technician.getId() + " is not a technician");
        }

        WorkOrderStatus previous = wo.getStatus();
        wo.setAssignedTo(technician);
        wo.setStatus(WorkOrderStatus.ASSIGNED);

        recordHistory(wo, previous, WorkOrderStatus.ASSIGNED, "Assigned to " + technician.getName());

        return WorkOrderResponse.from(wo);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public WorkOrderResponse changeStatus(UUID id, StatusUpdateRequest request) {
        WorkOrder wo = workOrderRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Work order not found: " + id));

        WorkOrderStatus from = wo.getStatus();
        WorkOrderStatus to = request.toStatus();

        if (to == WorkOrderStatus.ASSIGNED) {
            throw new IllegalArgumentException("Use POST /api/work-orders/{id}/assign to assign a technician");
        }

        if (!from.canTransitionTo(to)) {
            throw new IllegalStateException("Cannot transition from " + from + " to " + to);
        }

        authorizeTransition(wo, from, to);

        wo.setStatus(to);
        recordHistory(wo, from, to, request.note());

        return WorkOrderResponse.from(wo);
    }

    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    @Transactional(readOnly = true)
    public List<WorkOrderResponse> listOverdue() {
        return workOrderRepository.findOverdue(Instant.now()).stream()
                .map(WorkOrderResponse::from)
                .toList();
    }

    private void authorizeTransition(WorkOrder wo, WorkOrderStatus from, WorkOrderStatus to) {
        User currentUser = currentUser();
        boolean isDispatcherOrManager = currentUser.getRole() == Role.DISPATCHER || currentUser.getRole() == Role.MANAGER;
        boolean isAssignedTechnician = wo.getAssignedTo() != null && wo.getAssignedTo().getId().equals(currentUser.getId());

        boolean allowed = switch (to) {
            case CANCELLED -> isDispatcherOrManager;
            case CLOSED -> currentUser.getRole() == Role.MANAGER;
            case IN_PROGRESS -> (from == WorkOrderStatus.COMPLETED) ? isDispatcherOrManager : isAssignedTechnician;
            case ON_HOLD, COMPLETED -> isAssignedTechnician;
            default -> false;
        };

        if (!allowed) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You are not permitted to move this work order from " + from + " to " + to);
        }
    }

    private void recordHistory(WorkOrder wo, WorkOrderStatus from, WorkOrderStatus to, String note) {
        historyRepository.save(WorkOrderStatusHistory.builder()
                .workOrder(wo)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(currentUser())
                .note(note)
                .build());
    }

    private String generateUniqueCode() {
        String code;
        do {
            code = "WO-" + (100000 + RANDOM.nextInt(900000));
        } while (workOrderRepository.existsByCode(code));
        return code;
    }

    private User currentUser() {
        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}