package org.example.brokerage.service;

import org.example.brokerage.dto.CreateOrderRequest;
import org.example.brokerage.dto.OrderResponse;
import org.example.brokerage.exception.InsufficientBalanceException;
import org.example.brokerage.exception.OrderNotFoundException;
import org.example.brokerage.model.*;
import org.example.brokerage.repository.AssetRepository;
import org.example.brokerage.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.getOrderSide() == OrderSide.BUY) {
            handleBuyOrder(request);
        } else {
            handleSellOrder(request);
        }

        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .assetName(request.getAssetName())
                .orderSide(request.getOrderSide())
                .size(request.getSize())
                .price(request.getPrice())
                .status(OrderStatus.PENDING)
                .createDate(LocalDateTime.now())
                .build();

        order = orderRepository.save(order);
        return mapToResponse(order);
    }

    private void handleBuyOrder(CreateOrderRequest request) {
        BigDecimal totalCost = request.getSize().multiply(request.getPrice());

        Asset tryAsset = assetRepository
                .findByCustomerIdAndAssetName(request.getCustomerId(), "TRY")
                .orElseThrow(() -> new InsufficientBalanceException("TRY asset not found"));

        if (tryAsset.getUsableSize().compareTo(totalCost) < 0) {
            throw new InsufficientBalanceException("Insufficient TRY balance");
        }

        tryAsset.setUsableSize(tryAsset.getUsableSize().subtract(totalCost));
        assetRepository.save(tryAsset);
    }

    private void handleSellOrder(CreateOrderRequest request) {
        Asset asset = assetRepository
                .findByCustomerIdAndAssetName(request.getCustomerId(), request.getAssetName())
                .orElseThrow(() -> new InsufficientBalanceException("Asset not found"));

        if (asset.getUsableSize().compareTo(request.getSize()) < 0) {
            throw new InsufficientBalanceException("Insufficient asset balance");
        }

        asset.setUsableSize(asset.getUsableSize().subtract(request.getSize()));
        assetRepository.save(asset);
    }

    public List<OrderResponse> listOrders(Long customerId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders;
        if (startDate != null && endDate != null) {
            orders = orderRepository.findByCustomerIdAndCreateDateBetween(customerId, startDate, endDate);
        } else {
            orders = orderRepository.findByCustomerId(customerId);
        }
        return orders.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteOrder(Long orderId, Long customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (!order.getCustomerId().equals(customerId)) {
            throw new IllegalArgumentException("Order does not belong to customer");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal totalCost = order.getSize().multiply(order.getPrice());
            Asset tryAsset = assetRepository
                    .findByCustomerIdAndAssetName(customerId, "TRY")
                    .orElseThrow();
            tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalCost));
            assetRepository.save(tryAsset);
        } else {
            Asset asset = assetRepository
                    .findByCustomerIdAndAssetName(customerId, order.getAssetName())
                    .orElseThrow();
            asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
            assetRepository.save(asset);
        }

        order.setStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    private OrderResponse mapToResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .customerId(order.getCustomerId())
                .assetName(order.getAssetName())
                .orderSide(order.getOrderSide())
                .size(order.getSize())
                .price(order.getPrice())
                .status(order.getStatus())
                .createDate(order.getCreateDate())
                .build();
    }
}
