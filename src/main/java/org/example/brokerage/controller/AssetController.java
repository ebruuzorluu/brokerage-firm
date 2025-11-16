package org.example.brokerage.controller;

import org.example.brokerage.dto.AssetResponse;
import org.example.brokerage.security.UserPrincipal;
import org.example.brokerage.service.AssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/assets")
@RequiredArgsConstructor
public class AssetController {
    private final AssetService assetService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<AssetResponse>> listAssets(
            @RequestParam(required = false) Long customerId,
            @AuthenticationPrincipal UserPrincipal principal) {

        Long targetCustomerId = customerId;
        if (principal.getRole().equals("CUSTOMER")) {
            targetCustomerId = principal.getCustomerId();
        } else if (targetCustomerId == null) {
            return ResponseEntity.badRequest().build();
        }

        List<AssetResponse> assets = assetService.listAssets(targetCustomerId);
        return ResponseEntity.ok(assets);
    }
}
