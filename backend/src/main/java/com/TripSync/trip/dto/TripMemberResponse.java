package com.TripSync.trip.dto;

import com.TripSync.trip.entity.TripMemberRole;

import java.time.LocalDateTime;

public record TripMemberResponse (

        Long userId,
        String  fullName,
        String email,
        TripMemberRole role,
        LocalDateTime joinedAt
) {
}
