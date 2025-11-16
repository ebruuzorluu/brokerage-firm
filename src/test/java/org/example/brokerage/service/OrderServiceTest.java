package org.example.brokerage.service;

import org.example.brokerage.dto.CreateOrderRequest;
import org.example.brokerage.dto.OrderResponse;
import org.example.brokerage.exception.InsufficientBalanceException;
import org.example.brokerage.exception.OrderNotFoundException;
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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private OrderService orderService;

    private Asset tryAsset;
    private Asset stockAsset;

    @BeforeEach
    void setUp() {
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
    void createBuyOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setAssetName("AAPL");
        request.setOrderSide(OrderSide.BUY);
        request.setSize(new BigDecimal("5"));
        request.setPrice(new BigDecimal("100"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("9500"), tryAsset.getUsableSize());
        verify(assetRepository).save(tryAsset);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createBuyOrder_InsufficientBalance() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setAssetName("AAPL");
        request.setOrderSide(OrderSide.BUY);
        request.setSize(new BigDecimal("1000"));
        request.setPrice(new BigDecimal("100"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void createBuyOrder_TryAssetNotFound() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setAssetName("AAPL");
        request.setOrderSide(OrderSide.BUY);
        request.setSize(new BigDecimal("5"));
        request.setPrice(new BigDecimal("100"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void createSellOrder_Success() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setAssetName("AAPL");
        request.setOrderSide(OrderSide.SELL);
        request.setSize(new BigDecimal("5"));
        request.setPrice(new BigDecimal("100"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order order = invocation.getArgument(0);
                    order.setId(1L);
                    return order;
                });

        OrderResponse response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("5"), stockAsset.getUsableSize());
        verify(assetRepository).save(stockAsset);
    }

    @Test
    void createSellOrder_AssetNotFound() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setAssetName("AAPL");
        request.setOrderSide(OrderSide.SELL);
        request.setSize(new BigDecimal("5"));
        request.setPrice(new BigDecimal("100"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.empty());

        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void createSellOrder_InsufficientAssetBalance() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(1L);
        request.setAssetName("AAPL");
        request.setOrderSide(OrderSide.SELL);
        request.setSize(new BigDecimal("20"));
        request.setPrice(new BigDecimal("100"));

        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));

        assertThrows(InsufficientBalanceException.class, () -> {
            orderService.createOrder(request);
        });
    }

    @Test
    void listOrders_WithDateRange() {
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();

        Order order1 = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findByCustomerIdAndCreateDateBetween(1L, startDate, endDate))
                .thenReturn(Arrays.asList(order1));

        List<OrderResponse> orders = orderService.listOrders(1L, startDate, endDate);

        assertEquals(1, orders.size());
        assertEquals(1L, orders.get(0).getId());
    }

    @Test
    void listOrders_WithoutDateRange() {
        Order order1 = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findByCustomerId(1L))
                .thenReturn(Arrays.asList(order1));

        List<OrderResponse> orders = orderService.listOrders(1L, null, null);

        assertEquals(1, orders.size());
        assertEquals(1L, orders.get(0).getId());
    }

    @Test
    void deleteOrder_Success() {
        Order order = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "TRY"))
                .thenReturn(Optional.of(tryAsset));

        orderService.deleteOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELED, order.getStatus());
        verify(orderRepository).save(order);
        verify(assetRepository).save(tryAsset);
    }

    @Test
    void deleteOrder_SellOrder_Success() {
        Order order = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.SELL)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(assetRepository.findByCustomerIdAndAssetName(1L, "AAPL"))
                .thenReturn(Optional.of(stockAsset));

        orderService.deleteOrder(1L, 1L);

        assertEquals(OrderStatus.CANCELED, order.getStatus());
        assertEquals(new BigDecimal("15"), stockAsset.getUsableSize());
        verify(orderRepository).save(order);
        verify(assetRepository).save(stockAsset);
    }

    @Test
    void deleteOrder_OrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.deleteOrder(1L, 1L);
        });
    }

    @Test
    void deleteOrder_WrongCustomer() {
        Order order = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalArgumentException.class, () -> {
            orderService.deleteOrder(1L, 2L);
        });
    }

    @Test
    void deleteOrder_NotPending_ThrowsException() {
        Order order = Order.builder()
                .id(1L)
                .customerId(1L)
                .assetName("AAPL")
                .orderSide(OrderSide.BUY)
                .size(new BigDecimal("5"))
                .price(new BigDecimal("100"))
                .status(OrderStatus.MATCHED)
                .createDate(LocalDateTime.now())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(IllegalStateException.class, () -> {
            orderService.deleteOrder(1L, 1L);
        });
    }
}
