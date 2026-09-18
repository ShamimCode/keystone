package com.zidio.keystone.dto.site;

import java.time.Instant;
import java.util.UUID;

public record SiteResponse(
        UUID id,
        UUID customerId,
        String customerName,
        String name,
        String address,
        Instant createdAt
) {
    public static SiteResponse from(com.zidio.keystone.domain.Site site) {
        return new SiteResponse(
                site.getId(),
                site.getCustomer().getId(),
                site.getCustomer().getName(),
                site.getName(),
                site.getAddress(),
                site.getCreatedAt()
        );
    }
}
