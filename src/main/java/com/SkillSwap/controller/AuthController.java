package com.SkillSwap.controller;

import com.SkillSwap.model.User;
import com.SkillSwap.repository.UserRepository;
import com.SkillSwap.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private RegistrationService regService;
    @Autowired private UserRepository userRepo;
    @Autowired private PasswordEncoder passwordEncoder;

    @PostMapping("/signup")
    public ResponseEntity<String> register(@RequestBody User user, HttpServletRequest request) {
        String appUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        regService.registerUser(user, appUrl);
        return ResponseEntity.ok("Verification email sent!");
    }

    @GetMapping("/verify-email")
    public ResponseEntity<String> verify(@RequestParam String token) {
        boolean valid = regService.verifyToken(token);
        return valid ? ResponseEntity.ok("Email verified!") : ResponseEntity.badRequest().body("Invalid or expired token.");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> creds) {
        User user = (User) userRepo.findByEmail(creds.get("email")).orElse(null);
        if (user == null || !passwordEncoder.matches(creds.get("password"), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }
        if (!user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Email not verified.");
        }
        return ResponseEntity.ok("Login successful");
    }
}

