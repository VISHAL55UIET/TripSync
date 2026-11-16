package com.TripSync.ai.service;

import com.TripSync.ai.dto.TripGuideRequest;
import com.TripSync.ai.dto.TripGuideResponse;
import com.TripSync.trip.entity.Trip;
import com.TripSync.trip.repository.TripMemberRepository;
import com.TripSync.trip.repository.TripRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AiTravelAdvisorService {

    private final TripRepository tripRepository;
    private final TripMemberRepository tripMemberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient = RestClient.create();

    @Value("${GEMINI_API_KEY:}")
    private String geminiApiKey;

    public TripGuideResponse generateTripGuide(Long tripId, TripGuideRequest request) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "GEMINI_API_KEY is not configured");
        }

        String destination = request.destination();
        String seasonInfo = request.seasonOrMonth();
        int groupSize = request.groupSize() != null && request.groupSize() > 0 ? request.groupSize() : 2;
        long days = 4;

        if (tripId != null) {
            Trip trip = tripRepository.findById(tripId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
            if (destination == null || destination.isBlank()) {
                destination = trip.getDestination();
            }
            if (seasonInfo == null || seasonInfo.isBlank()) {
                seasonInfo = trip.getStartDate().getMonth() + " (" + trip.getStartDate() + " to " + trip.getEndDate() + ")";
            }
            int memberCount = tripMemberRepository.findByTrip_Id(tripId).size();
            if (request.groupSize() == null && memberCount > 0) {
                groupSize = memberCount;
            }
            days = Math.max(1, ChronoUnit.DAYS.between(trip.getStartDate(), trip.getEndDate()) + 1);
        }

        String budgetText = request.totalBudget() != null
                ? "₹" + request.totalBudget() + " total for the group"
                : "Moderate budget-friendly Indian traveler budget";

        String prompt = """
                You are an expert Indian travel planner for TripSync.
                Analyze this trip and reply in friendly, practical English/Hinglish tone:
                - Destination: %s
                - Travel Month / Dates: %s (%d days)
                - Group Size: %d people
                - Budget: %s
                - Extra User Note: %s

                Return ONLY valid JSON matching this exact structure (no markdown backticks):
                {
                  "seasonVerdict": "Whether this destination is ideal in this season/weather (winter/summer/monsoon) and why",
                  "weatherAndPackingTip": "Expected temperature and 2-3 must-pack items",
                  "budgetBreakdown": "Practical per-person and group split for stay, local transport, food, and how many spots they can comfortably cover in this budget",
                  "topPlaces": [
                    { "name": "Spot name", "bestTimeToVisit": "Morning/Evening", "estimatedCost": "₹...", "whyGo": "1-line highlight" }
                  ],
                  "mustTryFood": [
                    { "dishName": "Local dish", "whereToTry": "Famous area/cafe", "approxPrice": "₹..." }
                  ],
                  "alternativeDestinations": [
                    "2-3 alternative destinations in India that are even better in this exact season & budget"
                  ]
                }
                """.formatted(
                destination,
                seasonInfo,
                days,
                groupSize,
                budgetText,
                request.customQuestion() != null ? request.customQuestion() : "None"
        );

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of("responseMimeType", "application/json")
        );

        try {
            String rawResponse = restClient.post()
                    .uri("https://generativelanguage.googleapis.com/v1beta/models/gemini-flash-latest:generateContent?key=" + geminiApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(rawResponse);
            String jsonText = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return objectMapper.readValue(jsonText, TripGuideResponse.class);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "AI Travel Advisor failed: " + e.getMessage());
        }
    }
}