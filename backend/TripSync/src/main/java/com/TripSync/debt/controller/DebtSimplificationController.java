package com.TripSync.debt.controller;

import com.TripSync.debt.dto.SimplifiedDebtResponse;
import com.TripSync.debt.service.DebtSimplificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/debts")
public class DebtSimplificationController {

    private final DebtSimplificationService debtSimplificationService;

    @GetMapping("/simplify")
    public ResponseEntity<List<SimplifiedDebtResponse>> getSimplifiedDebts(@PathVariable Long tripId) {
        return ResponseEntity.ok(debtSimplificationService.simplifyDebts(tripId));
    }
}