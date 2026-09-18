package com.zidio.keystone.dto.workorder;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AssignRequest(
        @NotNull UUID technicianId
) {
}
