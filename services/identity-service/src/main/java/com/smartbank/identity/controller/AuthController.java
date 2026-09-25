package com.smartbank.identity.controller;

import com.smartbank.identity.dto.AuthResponse;
import com.smartbank.identity.dto.ChangePasswordRequest;
import com.smartbank.identity.dto.GoogleAuthRequest;
import com.smartbank.identity.dto.LoginRequest;
import com.smartbank.identity.dto.RegisterRequest;
import com.smartbank.identity.dto.UpdateProfileRequest;
import com.smartbank.identity.security.JwtService;
import com.smartbank.identity.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AuthResponse> register(
            @RequestParam("username") String username,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar) {

        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setEmail(email);
        req.setPassword(password);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(req, avatar));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleAuthRequest request) {
        return ResponseEntity.ok(authService.googleSignIn(request.getIdToken()));
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Missing or invalid Authorization header"));
        }

        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("valid", false, "error", "Invalid or expired token"));
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "claims", jwtService.parseToken(token)
        ));
    }

    // ---- NEW: Get current user profile ----
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> me(@RequestHeader("Authorization") String authHeader) {
        String userId = extractUserId(authHeader);
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }

    // ---- NEW: Update profile ----
    @PatchMapping("/me")
    public ResponseEntity<AuthResponse> updateProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateProfileRequest request) {
        String userId = extractUserId(authHeader);
        return ResponseEntity.ok(authService.updateProfile(userId, request));
    }

    // ---- NEW: Change password ----
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ChangePasswordRequest request) {
        String userId = extractUserId(authHeader);
        authService.changePassword(userId, request);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    private String extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Missing Authorization header");
        }
        String token = authHeader.substring(7);
        if (!jwtService.isValid(token)) {
            throw new RuntimeException("Invalid or expired token");
        }
        return jwtService.parseToken(token).get("userId", String.class);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleError(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }
}