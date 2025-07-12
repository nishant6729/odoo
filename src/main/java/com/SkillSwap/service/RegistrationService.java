package com.SkillSwap.service;


import com.SkillSwap.repository.VerificationTokenRepository;
import org.springframework.mail.SimpleMailMessage;
import com.SkillSwap.model.User;
import com.SkillSwap.model.VerificationToken;
import com.SkillSwap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
public class RegistrationService {

    @Autowired
    private UserRepository userRepo;
    @Autowired private VerificationTokenRepository tokenRepo;
    @Autowired private JavaMailSender mailSender;
    @Autowired private PasswordEncoder passwordEncoder;

    public void registerUser(User user, String appUrl) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEmailVerified(false);
        userRepo.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        tokenRepo.save(verificationToken);

        String link = appUrl + "/verify-email?token=" + token;
        sendEmail(user.getEmail(), link);
    }

    private void sendEmail(String to, String link) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verify your email");
        message.setText("Click the link to verify your email: " + link);
        mailSender.send(message);
    }

    public boolean verifyToken(String token) {
        VerificationToken vToken = (VerificationToken) tokenRepo.findByToken(token).orElse(null);
        if (vToken == null || vToken.getExpiryDate().isBefore(ChronoLocalDateTime.from(Instant.from(LocalDateTime.now())))) {
            return false;
        }
        User user = vToken.getUser();
        user.setEmailVerified(true);
        userRepo.save(user);
        tokenRepo.delete(vToken);
        return true;
    }
}
