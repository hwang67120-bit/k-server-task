package com.example.kservertask.order.repository;

import com.example.kservertask.order.entity.CoffeeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<CoffeeOrder, Long> {
}
