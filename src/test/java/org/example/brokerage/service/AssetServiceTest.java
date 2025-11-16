package org.example.brokerage.service;

import org.example.brokerage.dto.AssetResponse;
import org.example.brokerage.model.Asset;
import org.example.brokerage.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    void listAssets_Success() {
        Asset asset1 = Asset.builder()
                .id(1L)
                .customerId(1L)
                .assetName("TRY")
                .size(new BigDecimal("10000"))
                .usableSize(new BigDecimal("10000"))
                .build();

        Asset asset2 = Asset.builder()
                .id(2L)
                .customerId(1L)
                .assetName("AAPL")
                .size(new BigDecimal("10"))
                .usableSize(new BigDecimal("10"))
                .build();

        when(assetRepository.findByCustomerId(1L))
                .thenReturn(Arrays.asList(asset1, asset2));

        List<AssetResponse> responses = assetService.listAssets(1L);

        assertEquals(2, responses.size());
        assertEquals("TRY", responses.get(0).getAssetName());
        assertEquals("AAPL", responses.get(1).getAssetName());
    }
}
