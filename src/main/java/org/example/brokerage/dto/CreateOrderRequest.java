package org.example.brokerage.dto;

import org.example.brokerage.model.OrderSide;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotBlank(message = "Asset name is required")
    private String assetName;

    @NotNull(message = "Order side is required")
    private OrderSide orderSide;

    @NotNull(message = "Size is required")
    @DecimalMin(value = "0.01", message = "Size must be greater than 0")
    private BigDecimal size;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;
}
