package com.TripSync.memory.dto;

import java.time.LocalDateTime;
import java.util.List;

public record MemoryResponse(
        Long id,
        String imageUrl,
        String caption,
        String locationTag,
        String uploadedByName,
        String uploadedByEmail,
        LocalDateTime createdAt,
        int likeCount,
        boolean likedByCurrentUser,
        List<String> likedByNames
) {}