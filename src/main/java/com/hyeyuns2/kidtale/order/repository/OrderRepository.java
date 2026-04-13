package com.hyeyuns2.kidtale.order.repository;

import com.hyeyuns2.kidtale.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByStoryId(Long storyId);
}
