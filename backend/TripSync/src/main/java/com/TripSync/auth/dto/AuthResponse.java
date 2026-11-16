package com.TripSync.auth.dto;

public record AuthResponse(

        String accessToken,
        String tokenType,
        Long expiresIn
) {
}