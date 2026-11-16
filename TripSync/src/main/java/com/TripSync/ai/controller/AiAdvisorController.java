package com.TripSync.ai.controller;

import com.TripSync.ai.dto.TripGuideRequest;
import com.TripSync.ai.dto.TripGuideResponse;
import com.TripSync.ai.service.AiTravelAdvisorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class AiAdvisorController {

    private final AiTravelAdvisorService aiTravelAdvisorService;

    // 1. Trip ke andar (auto-reads destination, dates, and member count)
    @PostMapping("/trips/{tripId}/ai/guide")
    public ResponseEntity<TripGuideResponse> getTripGuide(
            @PathVariable Long tripId,
            @RequestBody(required = false) TripGuideRequest request
    ) {
        TripGuideRequest safeReq = request != null
                ? request
                : new TripGuideRequest(null, null, null, null, null);
        return ResponseEntity.ok(aiTravelAdvisorService.generateTripGuide(tripId, safeReq));
    }

    // 2. Dashboard par trip plan karne se pehle ("Summer/Winter + Budget + People" finder)
    @PostMapping("/ai/discover")
    public ResponseEntity<TripGuideResponse> discoverDestination(@RequestBody TripGuideRequest request) {
        return ResponseEntity.ok(aiTravelAdvisorService.generateTripGuide(null, request));
    }
}