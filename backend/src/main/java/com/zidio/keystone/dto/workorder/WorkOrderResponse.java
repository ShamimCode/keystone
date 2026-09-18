package com.zidio.keystone.dto.workorder;

import com.zidio.keystone.domain.WorkOrder;
import com.zidio.keystone.domain.enums.Priority;
import com.zidio.keystone.domain.enums.WorkOrderStatus;

import java.time.Instant;
import java.util.UUID;

public record WorkOrderResponse(
        UUID id,
        String code,
        String title,
        String description,
        Priority priority,
        WorkOrderStatus status,
        UUID customerId,
        String customerName,
        UUID siteId,
        String siteName,
        UUID assignedToId,
        String assignedToName,
        Instant slaDueAt,
        Instant createdAt,
        Instant updatedAt
) {
    public static WorkOrderResponse from(WorkOrder wo) {
        return new WorkOrderResponse(
                wo.getId(),
                wo.getCode(),
                wo.getTitle(),
                wo.getDescription(),
                wo.getPriority(),
                wo.getStatus(),
                wo.getCustomer().getId(),
                wo.getCustomer().getName(),
                wo.getSite().getId(),
                wo.getSite().getName(),
                wo.getAssignedTo() != null ? wo.getAssignedTo().getId() : null,
                wo.getAssignedTo() != null ? wo.getAssignedTo().getName() : null,
                wo.getSlaDueAt(),
                wo.getCreatedAt(),
                wo.getUpdatedAt()
        );
    }
}