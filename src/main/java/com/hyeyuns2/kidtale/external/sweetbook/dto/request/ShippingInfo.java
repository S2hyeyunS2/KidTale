package com.hyeyuns2.kidtale.external.sweetbook.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ShippingInfo(
        String recipientName,
        String recipientPhone,
        String postalCode,
        String address1,
        String address2,
        String memo
) {}
