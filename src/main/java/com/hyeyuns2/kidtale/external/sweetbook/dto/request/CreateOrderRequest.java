package com.hyeyuns2.kidtale.external.sweetbook.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateOrderRequest(
        List<OrderItem> items,
        ShippingInfo shipping,
        String externalRef
) {}
