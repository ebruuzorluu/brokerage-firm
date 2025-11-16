package org.example.brokerage.dto;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
public class AssetResponse {
    private Long id;
    private Long customerId;
    private String assetName;
    private BigDecimal size;
    private BigDecimal usableSize;
}
