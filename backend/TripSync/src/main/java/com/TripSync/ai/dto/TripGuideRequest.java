package com.TripSync.ai.dto;

import java.math.BigDecimal;

public record TripGuideRequest(
        String destination,
        String seasonOrMonth,
        Integer groupSize,
        BigDecimal totalBudget,
        String customQuestion
) {}