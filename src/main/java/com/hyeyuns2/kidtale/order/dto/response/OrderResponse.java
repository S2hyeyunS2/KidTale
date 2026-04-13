package com.hyeyuns2.kidtale.order.dto.response;

import com.hyeyuns2.kidtale.order.entity.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long storyId,
        String sweetBookOrderId,
        String recipientName,
        String recipientPhone,
        String postalCode,
        String address1,
        String address2,
        String memo,
        int quantity,
        OrderStatus status,
        LocalDateTime createdAt
) {}
