package org.example.brokerage.controller;

import org.example.brokerage.dto.CreateOrderRequest;
import org.example.brokerage.dto.OrderResponse;
import org.example.brokerage.security.UserPrincipal;
import org.example.brokerage.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<OrderResponse> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal.getRole().equals("CUSTOMER") &&
                !request.getCustomerId().equals(principal.getCustomerId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        OrderResponse response = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<OrderResponse>> listOrders(
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long targetCustomerId = customerId;
        if (principal.getRole().equals("CUSTOMER")) {
            targetCustomerId = principal.getCustomerId();
        } else if (targetCustomerId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<OrderResponse> orders = orderService.listOrders(targetCustomerId, startDate, endDate);
        return ResponseEntity.ok(orders);
    }

    @DeleteMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserPrincipal principal) {

        orderService.deleteOrder(orderId, principal.getCustomerId());
        return ResponseEntity.noContent().build();
    }
}
