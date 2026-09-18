package com.zidio.keystone.controller;

import com.zidio.keystone.dto.site.SiteRequest;
import com.zidio.keystone.dto.site.SiteResponse;
import com.zidio.keystone.service.SiteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;

    @PostMapping
    public ResponseEntity<SiteResponse> create(@Valid @RequestBody SiteRequest request) {
        SiteResponse response = siteService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{id}")
    public  ResponseEntity<SiteResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(siteService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<SiteResponse>> list(
            @RequestParam(required = false) UUID customerId,
            Pageable pageable
    ) {
        if (customerId != null) {
            return ResponseEntity.ok(siteService.listByCustomer(customerId, pageable));
        }

        return ResponseEntity.ok(siteService.list(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SiteResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody SiteRequest request
    ) {
        return ResponseEntity.ok(siteService.update(id, request));
    }
}
