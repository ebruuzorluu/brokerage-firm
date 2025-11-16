package org.example.brokerage.service;

import org.example.brokerage.dto.AssetResponse;
import org.example.brokerage.model.Asset;
import org.example.brokerage.repository.AssetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetService {
    private final AssetRepository assetRepository;

    public List<AssetResponse> listAssets(Long customerId) {
        return assetRepository.findByCustomerId(customerId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AssetResponse mapToResponse(Asset asset) {
        return AssetResponse.builder()
                .id(asset.getId())
                .customerId(asset.getCustomerId())
                .assetName(asset.getAssetName())
                .size(asset.getSize())
                .usableSize(asset.getUsableSize())
                .build();
    }
}
