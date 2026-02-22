package com.clinicore.project.service.impl;

import com.clinicore.project.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public SmtpEmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendActivationCode(String toEmail, String activationCode) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("CliniCore Activation Code: Complete Account Creation");
        msg.setText("""
                Your CliniCore activation code is:

                %s

                Do not share this with anyone.
                Copy your activation code, and use the link below to create your account:
                %s/activate-account

                If you did not request this, you can ignore this email.
                """.formatted(activationCode, frontendUrl));

        mailSender.send(msg);
    }

    @Override
    public void sendAccountCreatedConfirmation(String toEmail, String fullName, String username, String role) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("CliniCore Account Successfully Created");
        msg.setText("""
                Your CliniCore account has been successfully created.

                Name: %s
                Username: %s
                Role: %s

                You can now log in at:
                %s

                Use the username and password set during account creation to log in.
                """.formatted(fullName, username, role, frontendUrl));

        mailSender.send(msg);
    }

    @Override
    public void sendUsernameReminder(String toEmail, String username) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("CliniCore - Your Username");
        msg.setText("""
                You requested your CliniCore username.

                Your username is: %s

                You can log in at:
                %s

                If you did not request this, you can ignore this email.
                """.formatted(username, frontendUrl));

        mailSender.send(msg);
    }

    @Override
    public void sendPasswordResetLink(String toEmail, String resetLink) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("CliniCore - Reset Your Password");
        msg.setText("""
                You requested a password reset for your CliniCore account.

                Click the link below to reset your password:
                %s

                If you did not request this, you can ignore this email.
                """.formatted(resetLink));
        mailSender.send(msg);
    }
}