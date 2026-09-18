package com.zidio.keystone.controller;

import com.zidio.keystone.domain.enums.Role;
import com.zidio.keystone.dto.user.UserSummaryResponse;
import com.zidio.keystone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER')")
    public ResponseEntity<List<UserSummaryResponse>> listByRole(@RequestParam Role role) {
        List<UserSummaryResponse> users = userRepository.findAll().stream()
                .filter(u -> u.getRole() == role)
                .map(UserSummaryResponse::from)
                .toList();
        return ResponseEntity.ok(users);
    }
}