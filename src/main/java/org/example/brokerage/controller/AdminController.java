package org.example.brokerage.controller;

import lombok.RequiredArgsConstructor;
import org.example.brokerage.model.Order;
import org.example.brokerage.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/orders/{orderId}/match")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> matchOrder(@PathVariable Long orderId) {
        adminService.matchOrder(orderId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/orders/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getPendingOrders() {
        return ResponseEntity.ok(adminService.getPendingOrders());
    }
}
