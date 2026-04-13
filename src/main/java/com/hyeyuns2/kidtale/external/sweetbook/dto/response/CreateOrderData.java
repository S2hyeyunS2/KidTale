package com.hyeyuns2.kidtale.external.sweetbook.dto.response;

public record CreateOrderData(
        String orderUid,
        int orderStatus,
        String orderStatusDisplay,
        boolean isTest,
        long totalProductAmount,
        long totalShippingFee,
        long totalPackagingFee,
        long totalAmount,
        long paidCreditAmount,
        long creditBalanceAfter
) {}
