package com.TripSync.trip.dto;


import com.TripSync.trip.entity.TripMemberRole;
import jakarta.validation.constraints.NotNull;

public record UpdateMemberRoleRequest(

        @NotNull(message = "Member role is required")
        TripMemberRole role
) {
}