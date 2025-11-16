package org.example.brokerage.service;

import lombok.RequiredArgsConstructor;
import org.example.brokerage.model.Asset;
import org.example.brokerage.model.Order;
import org.example.brokerage.model.OrderSide;
import org.example.brokerage.model.OrderStatus;
import org.example.brokerage.repository.AssetRepository;
import org.example.brokerage.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final OrderRepository orderRepository;
    private final AssetRepository assetRepository;

    @Transactional
    public void matchOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be matched");
        }

        if (order.getOrderSide() == OrderSide.BUY) {
            matchBuyOrder(order);
        } else {
            matchSellOrder(order);
        }

        order.setStatus(OrderStatus.MATCHED);
        orderRepository.save(order);
    }

    private void matchBuyOrder(Order order) {
        BigDecimal totalCost = order.getSize().multiply(order.getPrice());

        // Deduct from TRY size
        Asset tryAsset = assetRepository
                .findByCustomerIdAndAssetName(order.getCustomerId(), "TRY")
                .orElseThrow();
        tryAsset.setSize(tryAsset.getSize().subtract(totalCost));
        assetRepository.save(tryAsset);

        // Add to asset
        Asset asset = assetRepository
                .findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                .orElse(Asset.builder()
                        .customerId(order.getCustomerId())
                        .assetName(order.getAssetName())
                        .size(BigDecimal.ZERO)
                        .usableSize(BigDecimal.ZERO)
                        .build());

        asset.setSize(asset.getSize().add(order.getSize()));
        asset.setUsableSize(asset.getUsableSize().add(order.getSize()));
        assetRepository.save(asset);
    }

    private void matchSellOrder(Order order) {
        BigDecimal totalRevenue = order.getSize().multiply(order.getPrice());

        // Deduct from asset size
        Asset asset = assetRepository
                .findByCustomerIdAndAssetName(order.getCustomerId(), order.getAssetName())
                .orElseThrow();
        asset.setSize(asset.getSize().subtract(order.getSize()));
        assetRepository.save(asset);

        // Add to TRY
        Asset tryAsset = assetRepository
                .findByCustomerIdAndAssetName(order.getCustomerId(), "TRY")
                .orElseThrow();
        tryAsset.setSize(tryAsset.getSize().add(totalRevenue));
        tryAsset.setUsableSize(tryAsset.getUsableSize().add(totalRevenue));
        assetRepository.save(tryAsset);
    }

    public List<Order> getPendingOrders() {
        return orderRepository.findByStatus(OrderStatus.PENDING);
    }
}
