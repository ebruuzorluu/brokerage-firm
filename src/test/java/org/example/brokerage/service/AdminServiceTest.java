package org.example.brokerage.service;

import org.example.brokerage.model.*;
import org.example.brokerage.repository.AssetRepository;
import org.example.brokerage.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AdminService adminService;

    private Order pendingBuyOrder;
    private Order pendingSellOrder;
    private Asset tryAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
        pendingBuyOrder = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        pendingSellOrder = Order.builder()
                .id(2L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.SELL)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .build();

        tryAsset = Asset.builder()
                .id(1L)
                .customerId(1L)
                .assetName("TRY")
                .size(new BigDecimal("10000"))
                .usableSize(new BigDecimal("10000"))
                .build();

        stockAsset = Asset.builder()
                .id(2L)
                .customerId(1L)
                .assetName("AAPL")
                .size(new BigDecimal("10"))
                .usableSize(new BigDecimal("10"))
                .build();
    }

    @Test
    void matchOrder_BuyOrder_NewAsset_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingBuyOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.empty());
        when(assetRepository.save(any(Asset.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        adminService.matchOrder(1L);

        assertEquals(OrderStatus.MATCHED, pendingBuyOrder.getStatus());
        assertEquals(new BigDecimal("9500"), tryAsset.getSize());
        verify(assetRepository, times(2)).save(any(Asset.class));
        verify(orderRepository).save(pendingBuyOrder);
    }

    @Test
    void matchOrder_BuyOrder_ExistingAsset_Success() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingBuyOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));

        adminService.matchOrder(1L);

        assertEquals(OrderStatus.MATCHED, pendingBuyOrder.getStatus());
        assertEquals(new BigDecimal("9500"), tryAsset.getSize());
        assertEquals(new BigDecimal("15"), stockAsset.getSize());
        assertEquals(new BigDecimal("15"), stockAsset.getUsableSize());
        verify(assetRepository, times(2)).save(any(Asset.class));
    }

    @Test
    void matchOrder_SellOrder_Success() {
        when(orderRepository.findById(2L)).thenReturn(Optional.of(pendingSellOrder));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        adminService.matchOrder(2L);

        assertEquals(OrderStatus.MATCHED, pendingSellOrder.getStatus());
        assertEquals(new BigDecimal("5"), stockAsset.getSize());
        assertEquals(new BigDecimal("10500"), tryAsset.getSize());
        assertEquals(new BigDecimal("10500"), tryAsset.getUsableSize());
        verify(assetRepository, times(2)).save(any(Asset.class));
    }

    @Test
    void matchOrder_OrderNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            adminService.matchOrder(999L);
        });
        verify(orderRepository, never()).save(any());
    }

    @Test
    void matchOrder_NotPendingStatus() {
        pendingBuyOrder.setStatus(OrderStatus.MATCHED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingBuyOrder));

        assertThrows(IllegalStateException.class, () -> {
            adminService.matchOrder(1L);
        });
        verify(orderRepository, never()).save(any());
    }

    @Test
    void matchOrder_CanceledStatus() {
        pendingBuyOrder.setStatus(OrderStatus.CANCELED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(pendingBuyOrder));

        assertThrows(IllegalStateException.class, () -> {
            adminService.matchOrder(1L);
        });
    }

    @Test
    void getPendingOrders_Success() {
        List<Order> pendingOrders = Arrays.asList(pendingBuyOrder, pendingSellOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(pendingOrders);

        List<Order> result = adminService.getPendingOrders();

        assertEquals(2, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
        assertEquals(OrderStatus.PENDING, result.get(1).getStatus());
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }

    @Test
    void getPendingOrders_EmptyList() {
        when(orderRepository.findByStatus(OrderStatus.PENDING))
                .thenReturn(Arrays.asList());

        List<Order> result = adminService.getPendingOrders();

        assertTrue(result.isEmpty());
        verify(orderRepository).findByStatus(OrderStatus.PENDING);
    }
}
