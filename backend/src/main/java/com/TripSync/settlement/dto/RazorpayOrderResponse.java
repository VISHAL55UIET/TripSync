package com.TripSync.settlement.dto;

import java.math.BigDecimal;

public record RazorpayOrderResponse(
        String orderId,
        long amountInPaise,
        String currency,
        String keyId,
        Long payeeId,
        String payeeName,
        BigDecimal amount,
        String note
) {}