package com.TripSync.debt.dto;

import java.math.BigDecimal;

public record SimplifiedDebtResponse(
        Long fromUserId,
        String fromUserName,
        Long toUserId,
        String toUserName,
        BigDecimal amount
) {}