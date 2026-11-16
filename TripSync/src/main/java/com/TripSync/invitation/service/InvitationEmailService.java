
package com.TripSync.invitation.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class InvitationEmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.invitation.base-url}")
    private String baseUrl;

    public InvitationEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendInvitation(
            String inviteeEmail,
            String tripTitle,
            String rawToken
    ) {

        String invitationUrl =
                baseUrl + "/invitations/accept?token=" + rawToken;

        String emailBody =
                "You have been invited to join the trip: "
                        + tripTitle + "\n\n"
                        + "Open the following link to review and accept "
                        + "the invitation:\n"
                        + invitationUrl + "\n\n"
                        + "This invitation expires after 48 hours.\n\n"
                        + "If you were not expecting this invitation, "
                        + "you can ignore this email.";

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(inviteeEmail);
        message.setSubject("TripSync - Trip Invitation");
        message.setText(emailBody);

        mailSender.send(message);
    }
}
