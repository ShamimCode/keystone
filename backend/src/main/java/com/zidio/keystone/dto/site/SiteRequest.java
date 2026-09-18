package com.zidio.keystone.dto.site;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SiteRequest(
        @NotNull UUID customerId,
        @NotBlank String name,
        @NotBlank String address
) {
}
