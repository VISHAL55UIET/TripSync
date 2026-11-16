package com.TripSync.memory.controller;

import com.TripSync.memory.dto.MemoryResponse;
import com.TripSync.memory.service.TripMemoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/trips/{tripId}/memories")
public class TripMemoryController {

    private final TripMemoryService memoryService;

    @GetMapping
    public ResponseEntity<List<MemoryResponse>> getMemories(
            @PathVariable Long tripId,
            Authentication auth
    ) {
        return ResponseEntity.ok(memoryService.getMemories(tripId, auth.getName()));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MemoryResponse> uploadMemory(
            @PathVariable Long tripId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "locationTag", required = false) String locationTag,
            Authentication auth
    ) throws IOException {
        return ResponseEntity.ok(
                memoryService.uploadMemory(tripId, file, caption, locationTag, auth.getName())
        );
    }

    @PostMapping("/{memoryId}/like")
    public ResponseEntity<MemoryResponse> toggleLike(
            @PathVariable Long tripId,
            @PathVariable Long memoryId,
            Authentication auth
    ) {
        return ResponseEntity.ok(memoryService.toggleLike(tripId, memoryId, auth.getName()));
    }

    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Void> deleteMemory(
            @PathVariable Long tripId,
            @PathVariable Long memoryId
    ) {
        memoryService.deleteMemory(tripId, memoryId);
        return ResponseEntity.noContent().build();
    }
}