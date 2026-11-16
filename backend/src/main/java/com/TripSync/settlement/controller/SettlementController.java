package com.TripSync.settlement.controller;

import com.TripSync.settlement.dto.CreateSettlementRequest;
import com.TripSync.settlement.dto.RazorpayOrderRequest;
import com.TripSync.settlement.dto.RazorpayOrderResponse;
import com.TripSync.settlement.dto.RazorpayVerifyRequest;
import com.TripSync.settlement.service.RazorpaySettlementService;
import com.TripSync.settlement.service.SettlementService;
import com.TripSync.user.entity.User;
import com.TripSync.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/settlements")
public class SettlementController {

    private final UserRepository userRepository;
    private final SettlementService settlementService;
    private final RazorpaySettlementService razorpaySettlementService;

    @PostMapping("/settlements")
    public ResponseEntity<Void> createSettlement(
            @PathVariable Long tripId,
            @Valid @RequestBody CreateSettlementRequest request,
            Authentication authentication
    ) {
        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        settlementService.createSettlement(tripId, currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/razorpay/order")
    public ResponseEntity<RazorpayOrderResponse> createRazorpayOrder(
            @PathVariable Long tripId,
            @Valid @RequestBody RazorpayOrderRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                razorpaySettlementService.createOrder(tripId, authentication.getName(), request)
        );
    }

    @PostMapping("/razorpay/verify")
    public ResponseEntity<Void> verifyRazorpayPayment(
            @PathVariable Long tripId,
            @Valid @RequestBody RazorpayVerifyRequest request,
            Authentication authentication
    ) {
        razorpaySettlementService.verifyAndRecordSettlement(tripId, authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}