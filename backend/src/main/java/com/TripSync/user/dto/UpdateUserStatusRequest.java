package com.TripSync.user.dto;

import com.TripSync.user.entity.AccountStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull(message = "Account status is required")
        AccountStatus status
) {
}
