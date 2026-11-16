package com.TripSync.ai.dto;

import java.util.List;

public record TripGuideResponse(
        String seasonVerdict,
        String weatherAndPackingTip,
        String budgetBreakdown,
        List<SpotRecommendation> topPlaces,
        List<FoodRecommendation> mustTryFood,
        List<String> alternativeDestinations
) {
    public record SpotRecommendation(
            String name,
            String bestTimeToVisit,
            String estimatedCost,
            String whyGo
    ) {}

    public record FoodRecommendation(
            String dishName,
            String whereToTry,
            String approxPrice
    ) {}
}