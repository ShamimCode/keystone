package com.zidio.keystone.dto.auth;

public record LoginResponse(
        String token,
        String email,
        String name,
        String role
) {}
