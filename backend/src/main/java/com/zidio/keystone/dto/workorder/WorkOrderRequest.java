package com.zidio.keystone.dto.workorder;

import com.zidio.keystone.domain.enums.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record WorkOrderRequest(
        @NotNull UUID customerId,
        @NotNull UUID siteId,
        @NotBlank String title,
        String description,
        @NotNull Priority priority
) {
}
