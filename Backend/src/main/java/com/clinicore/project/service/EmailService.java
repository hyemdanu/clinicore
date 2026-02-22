package com.clinicore.project.service;

public interface EmailService {
    void sendActivationCode(String toEmail, String activationCode);
    void sendAccountCreatedConfirmation(String toEmail, String fullName, String username, String role);
    void sendUsernameReminder(String toEmail, String username);
    void sendPasswordResetLink(String toEmail, String resetLink);
}