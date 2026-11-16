package com.TripSync.notification.service;

import com.TripSync.notification.dto.NotificationResponse;
import com.TripSync.notification.entity.NotificationType;
import com.TripSync.notification.entity.TripNotification;
import com.TripSync.notification.repository.TripNotificationRepository;
import com.TripSync.trip.entity.Trip;
import com.TripSync.user.entity.User;
import com.TripSync.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TripNotificationService {
    private final TripNotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void createNotification(User recipient, Trip trip, NotificationType type,
                                   String title, String message) {
        TripNotification notification = new TripNotification();
        notification.setRecipient(recipient);
        notification.setTrip(trip);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setRead(type == NotificationType.INVITATION_SENT);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(String email) {
        User user = getUser(email);
        return notificationRepository.findAllByRecipient_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId, String email) {
        User user = getUser(email);
        TripNotification notification = notificationRepository.findByIdAndRecipient_Id(notificationId, user.getId())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));
        notification.setRead(true);
        return toResponse(notificationRepository.save(notification));
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private NotificationResponse toResponse(TripNotification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTrip() != null ? notification.getTrip().getId() : null,
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}