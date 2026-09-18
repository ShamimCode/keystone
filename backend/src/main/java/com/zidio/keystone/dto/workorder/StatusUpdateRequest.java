package com.zidio.keystone.dto.workorder;

import com.zidio.keystone.domain.enums.WorkOrderStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull WorkOrderStatus toStatus,
        String note
) {
}