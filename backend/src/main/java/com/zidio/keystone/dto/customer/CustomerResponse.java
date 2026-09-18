package com.zidio.keystone.dto.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String name,
        String contactEmail,
        Instant createdAt
) {
    public static CustomerResponse from(com.zidio.keystone.domain.Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getName(),
                customer.getContactEmail(),
                customer.getCreatedAt()
        );
    }
}
