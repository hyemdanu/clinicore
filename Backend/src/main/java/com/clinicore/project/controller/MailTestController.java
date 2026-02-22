package com.clinicore.project.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// use this to test if ur email configs are setup
// use backend local port (not frontend)

@RestController
public class MailTestController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/test-email")
    public String sendTestEmail() {
        try {
            System.out.println(">>> HIT /test-email");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("olei.technical.user@gmail.com"); // must match SendGrid verified sender
            message.setTo("ongan@csus.edu");
            message.setSubject("ClinicoRe SMTP Test");
            message.setText("If you receive this, SMTP is working.");

            mailSender.send(message);

            return "Email sent successfully!";
        } catch (Exception e) {
            System.out.println(">>> fail /test-email");
            e.printStackTrace();  // <-- THIS prints full error in console
            return "Error: " + e.getMessage();
        }
    }
}