package com.TripSync.poll.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PollResponse(
        Long id,
        String question,
        boolean closed,
        String createdByName,
        Long createdById,
        LocalDateTime createdAt,
        int totalVotes,
        Long userVotedOptionId,
        List<OptionSummary> options
) {
    public record OptionSummary(
            Long id,
            String optionText,
            int voteCount,
            double percentage,
            List<String> voterNames
    ) {}
}