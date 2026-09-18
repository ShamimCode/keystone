package com.zidio.keystone.service;

import com.zidio.keystone.domain.Part;
import com.zidio.keystone.domain.PartUsage;
import com.zidio.keystone.domain.TimeLog;
import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.dto.workorder.PartUsageRequest;
import com.zidio.keystone.dto.workorder.TimeLogRequest;
import com.zidio.keystone.repository.PartRepository;
import com.zidio.keystone.repository.PartUsageRepository;
import com.zidio.keystone.repository.TimeLogRepository;
import com.zidio.keystone.repository.WorkOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final PartRepository partRepository;
    private final PartUsageRepository partUsageRepository;
    private final TimeLogRepository timeLogRepository;
    private final WorkOrderRepository workOrderRepository;

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void logPartUsage(UUID workOrderId, PartUsageRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NoSuchElementException("Work order not found: " + workOrderId));

        Part part = partRepository.findById(request.partId())
                .orElseThrow(() -> new NoSuchElementException("Part not found: " + request.partId()));

        if (part.getStockQty() < request.qtyUsed()) {
            throw new IllegalStateException(
                    "Insufficient stock for " + part.getName() + ": have " + part.getStockQty() + ", need " + request.qtyUsed());
        }

        part.setStockQty(part.getStockQty() - request.qtyUsed());

        partUsageRepository.save(PartUsage.builder()
                .workOrder(workOrder)
                .part(part)
                .qtyUsed(request.qtyUsed())
                .build());
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void logTime(UUID workOrderId, TimeLogRequest request) {
        WorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NoSuchElementException("Work order not found: " + workOrderId));

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        timeLogRepository.save(TimeLog.builder()
                .workOrder(workOrder)
                .technician(currentUser)
                .minutes(request.minutes())
                .note(request.note())
                .build());
    }
}