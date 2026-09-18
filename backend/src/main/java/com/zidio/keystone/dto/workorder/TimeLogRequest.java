package com.zidio.keystone.dto.workorder;

import jakarta.validation.constraints.Positive;

public record TimeLogRequest(
        @Positive Integer minutes,
        String note
) {
}
