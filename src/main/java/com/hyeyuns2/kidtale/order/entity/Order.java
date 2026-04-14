package com.hyeyuns2.kidtale.order.entity;

import com.hyeyuns2.kidtale.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private Long userId;

    @Column(nullable = false)
    private Long storyId;

    @Column(nullable = false, length = 100)
    private String sweetBookOrderId;

    @Column(nullable = false, length = 50)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false, length = 10)
    private String postalCode;

    @Column(nullable = false, length = 200)
    private String address1;

    @Column(length = 200)
    private String address2;

    @Column(length = 200)
    private String memo;

    @Column(nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    public static Order create(
            Long userId,
            Long storyId,
            String sweetBookOrderId,
            String recipientName,
            String recipientPhone,
            String postalCode,
            String address1,
            String address2,
            String memo,
            int quantity
    ) {
        Order order = new Order();
        order.userId = userId;
        order.storyId = storyId;
        order.sweetBookOrderId = sweetBookOrderId;
        order.recipientName = recipientName;
        order.recipientPhone = recipientPhone;
        order.postalCode = postalCode;
        order.address1 = address1;
        order.address2 = address2;
        order.memo = memo;
        order.quantity = quantity;
        order.status = OrderStatus.PENDING;
        return order;
    }
}
