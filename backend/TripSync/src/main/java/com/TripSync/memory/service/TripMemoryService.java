package com.TripSync.memory.service;

import com.TripSync.memory.dto.MemoryResponse;
import com.TripSync.memory.entity.MemoryLike;
import com.TripSync.memory.entity.TripMemory;
import com.TripSync.memory.repository.MemoryLikeRepository;
import com.TripSync.memory.repository.TripMemoryRepository;
import com.TripSync.realtime.service.TripRealtimePublisher;
import com.TripSync.trip.entity.Trip;
import com.TripSync.trip.repository.TripRepository;
import com.TripSync.user.entity.User;
import com.TripSync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TripMemoryService {

    private final TripRealtimePublisher realtimePublisher;
    private final TripMemoryRepository memoryRepository;
    private final MemoryLikeRepository likeRepository;
    private final TripRepository tripRepository;
    private final UserRepository userRepository;

    private static final Path UPLOAD_DIR = Paths.get("uploads", "memories");

    @Transactional(readOnly = true)
    public List<MemoryResponse> getMemories(Long tripId, String userEmail) {
        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return memoryRepository.findByTrip_IdOrderByCreatedAtDesc(tripId)
                .stream()
                .map(m -> toResponse(m, currentUser.getId()))
                .toList();
    }

    @Transactional
    public MemoryResponse uploadMemory(
            Long tripId,
            MultipartFile file,
            String caption,
            String locationTag,
            String userEmail
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Please select an image file");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files (JPG, PNG, WEBP) are allowed");
        }

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new RuntimeException("Trip not found"));
        User uploader = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Files.createDirectories(UPLOAD_DIR);

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "photo.jpg";
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.'))
                : ".jpg";
        String storedFileName = "trip_" + tripId + "_" + UUID.randomUUID() + ext;

        Path targetPath = UPLOAD_DIR.resolve(storedFileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        TripMemory memory = new TripMemory();
        memory.setTrip(trip);
        memory.setUploadedBy(uploader);
        memory.setImageUrl("/uploads/memories/" + storedFileName);
        memory.setCaption(caption != null && !caption.isBlank() ? caption.trim() : "Trip Memory");
        memory.setLocationTag(locationTag != null && !locationTag.isBlank() ? locationTag.trim() : trip.getDestination());

        TripMemory saved = memoryRepository.save(memory);

        realtimePublisher.publish(
                tripId,
                "MEMORY",
                "CREATED",
                uploader.getFullName() + " pinned a new photo memory: \"" + saved.getCaption() + "\"",
                userEmail
        );

        return toResponse(saved, uploader.getId());
    }

    @Transactional
    public MemoryResponse toggleLike(Long tripId, Long memoryId, String userEmail) {
        TripMemory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new RuntimeException("Memory not found"));
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        var existing = likeRepository.findByMemory_IdAndUser_Id(memoryId, user.getId());
        if (existing.isPresent()) {
            MemoryLike like = existing.get();
            memory.getLikes().remove(like);
            likeRepository.delete(like);
        } else {
            MemoryLike like = new MemoryLike();
            like.setMemory(memory);
            like.setUser(user);
            memory.getLikes().add(like);
            likeRepository.save(like);

            realtimePublisher.publish(
                    tripId,
                    "MEMORY",
                    "LIKED",
                    user.getFullName() + " liked \"" + memory.getCaption() + "\"",
                    userEmail
            );
        }

        return toResponse(memory, user.getId());
    }

    @Transactional
    public void deleteMemory(Long tripId, Long memoryId) {
        TripMemory memory = memoryRepository.findById(memoryId)
                .orElseThrow(() -> new RuntimeException("Memory not found"));

        if (memory.getImageUrl() != null && memory.getImageUrl().startsWith("/uploads/memories/")) {
            String fileName = memory.getImageUrl().replace("/uploads/memories/", "");
            try {
                Files.deleteIfExists(UPLOAD_DIR.resolve(fileName));
            } catch (IOException ignored) {}
        }

        memoryRepository.delete(memory);

        realtimePublisher.publish(
                tripId,
                "MEMORY",
                "DELETED",
                "A photo memory was removed from the wall",
                null
        );
    }

    private MemoryResponse toResponse(TripMemory m, Long currentUserId) {
        boolean likedByMe = m.getLikes().stream()
                .anyMatch(l -> l.getUser().getId().equals(currentUserId));

        List<String> likerNames = m.getLikes().stream()
                .map(l -> l.getUser().getFullName())
                .toList();

        return new MemoryResponse(
                m.getId(),
                m.getImageUrl(),
                m.getCaption(),
                m.getLocationTag(),
                m.getUploadedBy().getFullName(),
                m.getUploadedBy().getEmail(),
                m.getCreatedAt(),
                m.getLikes().size(),
                likedByMe,
                likerNames
        );
    }
}