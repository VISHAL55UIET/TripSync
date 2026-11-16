package com.TripSync.settlement.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record RazorpayVerifyRequest(
        @NotNull Long payeeId,
        @NotNull @DecimalMin("1.00") BigDecimal amount,
        @Size(max = 500) String note,
        @NotBlank String razorpayOrderId,
        @NotBlank String razorpayPaymentId,
        @NotBlank String razorpaySignature
) {}