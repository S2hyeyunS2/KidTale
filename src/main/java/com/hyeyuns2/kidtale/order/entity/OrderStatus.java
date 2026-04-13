package com.hyeyuns2.kidtale.order.entity;

public enum OrderStatus {
    PENDING,    // 주문 요청됨
    PAID,       // 결제 완료
    IN_PRINT,   // 인쇄 중
    SHIPPED,    // 배송 중
    DELIVERED,  // 배송 완료
    CANCELLED   // 취소됨
}
