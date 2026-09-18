package com.zidio.keystone.controller;

import com.zidio.keystone.dto.workorder.*;
import com.zidio.keystone.service.InventoryService;
import com.zidio.keystone.service.WorkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/work-orders")
@RequiredArgsConstructor
public class WorkOrderController {

    private final WorkOrderService workOrderService;
    private final InventoryService inventoryService;

    @PostMapping
    public ResponseEntity<WorkOrderResponse> create(@Valid @RequestBody WorkOrderRequest request) {
        WorkOrderResponse response = workOrderService.create(request);
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<WorkOrderResponse>> listOverdue() {
        return ResponseEntity.ok(workOrderService.listOverdue());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(workOrderService.getById(id));
    }

    @GetMapping
    public ResponseEntity<Page<WorkOrderResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(workOrderService.list(pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkOrderResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody WorkOrderRequest request) {
        return ResponseEntity.ok(workOrderService.update(id, request));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<WorkOrderResponse> assign(
            @PathVariable UUID id,
            @Valid @RequestBody AssignRequest request) {
        return ResponseEntity.ok(workOrderService.assign(id, request));
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<WorkOrderResponse> changeStatus(
            @PathVariable UUID id,
            @Valid @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(workOrderService.changeStatus(id, request));
    }

    @PostMapping("/{id}/parts")
    public ResponseEntity<Void> logPartUsage(
            @PathVariable UUID id,
            @Valid @RequestBody PartUsageRequest request) {
        inventoryService.logPartUsage(id, request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/{id}/time")
    public ResponseEntity<Void> logTime(
            @PathVariable UUID id,
            @Valid @RequestBody TimeLogRequest request) {
        inventoryService.logTime(id, request);
        return ResponseEntity.status(201).build();
    }
}
