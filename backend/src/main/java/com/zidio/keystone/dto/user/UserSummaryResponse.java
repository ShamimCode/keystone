package com.zidio.keystone.dto.user;

import com.zidio.keystone.domain.User;
import com.zidio.keystone.domain.enums.Role;

import java.util.UUID;

public record UserSummaryResponse(UUID id, String name, String email, Role role) {
    public static UserSummaryResponse from(User user) {
        return new UserSummaryResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}