package com.TripSync.poll.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreatePollRequest(
        @NotBlank(message = "Question is required")
        @Size(max = 300)
        String question,

        @NotEmpty(message = "At least 2 options are required")
        @Size(min = 2, max = 8, message = "Provide between 2 and 8 options")
        List<@NotBlank String> options
) {}