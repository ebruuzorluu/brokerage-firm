package org.example.brokerage.repository;

import org.example.brokerage.model.Order;
import org.example.brokerage.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByCustomerIdAndCreateDateBetween(
            Long customerId, LocalDateTime startDate, LocalDateTime endDate);
    List<Order> findByStatus(OrderStatus status);
}
