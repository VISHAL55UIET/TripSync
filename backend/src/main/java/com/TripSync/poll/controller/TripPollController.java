package com.TripSync.poll.controller;

import com.TripSync.poll.dto.CreatePollRequest;
import com.TripSync.poll.dto.PollResponse;
import com.TripSync.poll.service.TripPollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/polls")
public class TripPollController {

    private final TripPollService tripPollService;

    @GetMapping
    public ResponseEntity<List<PollResponse>> getPolls(
            @PathVariable Long tripId,
            Authentication auth
    ) {
        return ResponseEntity.ok(tripPollService.getTripPolls(tripId, auth.getName()));
    }

    @PostMapping
    public ResponseEntity<PollResponse> createPoll(
            @PathVariable Long tripId,
            @Valid @RequestBody CreatePollRequest request,
            Authentication auth
    ) {
        return ResponseEntity.ok(tripPollService.createPoll(tripId, request, auth.getName()));
    }

    @PostMapping("/{pollId}/vote/{optionId}")
    public ResponseEntity<PollResponse> vote(
            @PathVariable Long tripId,
            @PathVariable Long pollId,
            @PathVariable Long optionId,
            Authentication auth
    ) {
        return ResponseEntity.ok(tripPollService.castVote(tripId, pollId, optionId, auth.getName()));
    }

    @PatchMapping("/{pollId}/toggle-close")
    public ResponseEntity<PollResponse> toggleClose(
            @PathVariable Long tripId,
            @PathVariable Long pollId,
            Authentication auth
    ) {
        return ResponseEntity.ok(tripPollService.toggleClosePoll(tripId, pollId, auth.getName()));
    }

    @DeleteMapping("/{pollId}")
    public ResponseEntity<Void> deletePoll(
            @PathVariable Long tripId,
            @PathVariable Long pollId
    ) {
        tripPollService.deletePoll(tripId, pollId);
        return ResponseEntity.noContent().build();
    }
}