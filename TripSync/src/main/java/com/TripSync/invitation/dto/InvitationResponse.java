package com.TripSync.invitation.dto;


import com.TripSync.invitation.entity.InvitationStatus;
import com.TripSync.trip.entity.TripMemberRole;

import java.time.LocalDateTime;

public record InvitationResponse(
        Long invitationId,
        Long tripId,
        String inviteeEmail,
        TripMemberRole proposedRole,
        InvitationStatus status,
        LocalDateTime expiresAt

) {
}