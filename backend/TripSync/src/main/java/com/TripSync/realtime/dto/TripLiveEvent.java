package com.TripSync.realtime.dto;

import java.time.LocalDateTime;

public record TripLiveEvent(
        Long tripId,
        String module,      // "POLL", "MEMORY", "ITINERARY", "EXPENSE", "SETTLEMENT"
        String action,      // "CREATED", "VOTED", "UPDATED", "DELETED", "LIKED"
        String message,     // e.g., "Shivam voted on 'food'"
        String actorEmail,
        LocalDateTime timestamp
) {}