package com.TripSync.realtime.service;

import com.TripSync.realtime.dto.TripLiveEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TripRealtimePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public void publish(Long tripId, String module, String action, String message, String actorEmail) {
        if (tripId == null) return;
        TripLiveEvent event = new TripLiveEvent(
                tripId,
                module,
                action,
                message,
                actorEmail,
                LocalDateTime.now()
        );
        String destination = "/topic/trips/" + tripId;
        messagingTemplate.convertAndSend(destination, event);
        log.debug("Broadcasted live event to {}: {}", destination, event);
    }
}