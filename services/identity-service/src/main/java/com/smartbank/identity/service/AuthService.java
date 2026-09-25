package com.smartbank.identity.service;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.smartbank.identity.dto.AuthResponse;
import com.smartbank.identity.dto.LoginRequest;
import com.smartbank.identity.dto.RegisterRequest;
import com.smartbank.identity.entity.User;
import com.smartbank.identity.repository.UserRepository;
import com.smartbank.identity.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import com.smartbank.identity.dto.ChangePasswordRequest;
import com.smartbank.identity.dto.UpdateProfileRequest;
import java.util.Collections;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FileStorageService fileStorageService;

    @Value("${google.client-id:}")
    private String googleClientId;

    public AuthService(UserRepository repository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       FileStorageService fileStorageService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.fileStorageService = fileStorageService;
    }

    // ---------------- LOCAL SIGNUP ----------------

    @Transactional
    public AuthResponse register(RegisterRequest request, MultipartFile avatarFile) {
        if (repository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already taken");
        }
        if (repository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("CUSTOMER");
        user.setProvider("LOCAL");
        user.setDisplayName(request.getUsername());

        // Optional profile picture
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String url = fileStorageService.saveAvatar(avatarFile);
            user.setProfilePictureUrl(url);
        }

        User saved = repository.save(user);
        return buildAuthResponse(saved);
    }

    // ---------------- LOCAL LOGIN ----------------

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = repository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid username or password"));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new RuntimeException(
                    "This account uses Google sign-in. Please continue with Google.");
        }
        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid username or password");
        }

        return buildAuthResponse(user);
    }

    // ---------------- GOOGLE SIGN-IN ----------------

    @Transactional
    public AuthResponse googleSignIn(String idTokenString) {
        if (googleClientId == null || googleClientId.isBlank()) {
            throw new RuntimeException("Google sign-in is not configured");
        }

        GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);

        String email = payload.getEmail();
        String name  = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        User user = repository.findByEmail(email).orElse(null);

        if (user == null) {
            // First-time Google user — create account
            user = new User();
            user.setEmail(email);
            user.setUsername(generateUniqueUsername(email));
            user.setRole("CUSTOMER");
            user.setProvider("GOOGLE");
            user.setDisplayName(name != null ? name : email);
            user.setProfilePictureUrl(picture);
            user = repository.save(user);
            log.info("Created new user from Google: {}", email);
        } else if (!"GOOGLE".equals(user.getProvider())) {
            // Existing local user — allow them to link Google on first use
            user.setProvider("GOOGLE");
            if (user.getProfilePictureUrl() == null) {
                user.setProfilePictureUrl(picture);
            }
            user = repository.save(user);
            log.info("Linked existing user with Google: {}", email);
        }

        return buildAuthResponse(user);
    }

    // ---------------- HELPERS ----------------

    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new RuntimeException("Invalid Google token");
            }
            return idToken.getPayload();
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new RuntimeException("Google sign-in verification failed");
        }
    }

    private String generateUniqueUsername(String email) {
        String base = email.split("@")[0].replaceAll("[^a-zA-Z0-9]", "");
        if (base.isBlank()) base = "user";
        String candidate = base;
        int i = 1;
        while (repository.existsByUsername(candidate)) {
            candidate = base + i++;
        }
        return candidate;
    }

    private AuthResponse buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        AuthResponse response = new AuthResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getProfilePictureUrl(),
                user.getDisplayName(),
                86400000L
        );
        response.setKycVerified(user.isKycVerified());
        return response;
    }
    // ---- Get current user ----
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String userId) {
        User user = repository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildAuthResponse(user);
    }

    // ---- Update profile ----
    @Transactional
    public AuthResponse updateProfile(String userId, UpdateProfileRequest request) {
        User user = repository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getDisplayName() != null && !request.getDisplayName().isBlank()) {
            user.setDisplayName(request.getDisplayName().trim());
        }

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            String newEmail = request.getEmail().trim().toLowerCase();
            if (!newEmail.equals(user.getEmail()) && repository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already in use");
            }
            user.setEmail(newEmail);
        }

        User saved = repository.save(user);
        return buildAuthResponse(saved);
    }
    // ---- Change password ----
    @Transactional
    public void changePassword(String userId, ChangePasswordRequest request) {
        User user = repository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"LOCAL".equals(user.getProvider())) {
            throw new RuntimeException("Password change not available for Google accounts");
        }

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }
}