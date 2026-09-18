package com.zidio.keystone.dto.workorder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PartUsageRequest(
        @NotNull UUID partId,
        @Positive Integer qtyUsed
) {
}
