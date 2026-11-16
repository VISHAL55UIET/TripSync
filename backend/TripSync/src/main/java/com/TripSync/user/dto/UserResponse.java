
package com.TripSync.user.dto;

import com.TripSync.user.entity.AccountStatus;
import com.TripSync.user.entity.Role;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        Role role,
        AccountStatus status
) {
}
