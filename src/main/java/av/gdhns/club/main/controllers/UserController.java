package av.gdhns.club.main.controllers;

import av.gdhns.club.main.helpers.Mail;
import av.gdhns.club.main.model.UserRegistrationModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static av.gdhns.club.main.helpers.Mail.hash;

@RestController
@RequestMapping("/registration_member")
public class UserController {
    private static final String SUPABASE_URL = "https://pmzkotxaodkqihopptti.supabase.co";
    private static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBtemtvdHhhb2RrcWlob3BwdHRpIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjAzNjMyNzEsImV4cCI6MjA3NTkzOTI3MX0.zzNqFU0nPdjxeMP3YAbzJKr_7Ra_b_BP03HLM9kECAg";
    private static final String SUPABASE_STORAGE_BUCKET = "user-files";
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("member_registrations");
    private final WebClient webClient;

    public UserController() {
        this.webClient = WebClient.builder()
                .baseUrl(SUPABASE_URL)
                .defaultHeader("apikey", SUPABASE_KEY)
                .defaultHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();
    }

    @PostMapping
    public Mono<ResponseEntity<String>> createMemberRegistration(
            @RequestParam("data") String userJson,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "video", required = false) MultipartFile video) {

        if (userJson == null || userJson.isEmpty()) {
            return Mono.just(ResponseEntity.status(400).body("Error: userJson is required"));
        }

        try {
            UserRegistrationModel registration = new ObjectMapper().readValue(userJson, UserRegistrationModel.class);
            registration.setCreatedAt(Instant.now().toString());

            String otp = Mail.generateOTP();
            boolean sent = Mail.sendOTP(registration.getEmail(), otp);
            if (!sent) {
                return Mono.just(ResponseEntity.status(500).body("Error: Failed to send OTP"));
            }
            if (!storeOtp(userRef, registration.getEmail(), otp)) {
                return Mono.just(ResponseEntity.status(500).body("Error: Failed to save OTP"));
            }

            return uploads(registration, photo, video);
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just(ResponseEntity.status(500).body("Error: " + e.getMessage()));
        }
    }

    public Mono<String> uploadToSupabase(File file, String contentType, String fileName) {
        System.out.println("Uploading file: " + fileName + " with content type: " + contentType);

        try {
            byte[] fileBytes = Files.readAllBytes(file.toPath());
            return webClient.put()
                    .uri("/storage/v1/object/" + SUPABASE_STORAGE_BUCKET + "/" + fileName)
                    .contentType(MediaType.parseMediaType(contentType))
                    .bodyValue(fileBytes)
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> response.bodyToMono(String.class)
                            .flatMap(errorBody -> {
                                System.err.println("Supabase error: " + response.statusCode() + " - " + errorBody);
                                return Mono.error(new IOException("Supabase upload failed: " + errorBody));
                            }))
                    .bodyToMono(String.class)
                    .map(response -> getPublicUrl(fileName));
        } catch (IOException e) {
            return Mono.error(new IOException("Failed to read file: " + e.getMessage(), e));
        }
    }

    public String getPublicUrl(String fileName) {
        return SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_STORAGE_BUCKET + "/" + fileName;
    }

    private Mono<ResponseEntity<String>> uploads(UserRegistrationModel registration,
                                                 MultipartFile photo,
                                                 MultipartFile video) {

        final StringBuilder response = new StringBuilder();

        Mono<String> photoLinkMono = Mono.justOrEmpty(photo)
                .filter(p -> !p.isEmpty())
                .flatMap(p -> {
                    String photoFileName = "photo_" + System.currentTimeMillis() +
                            (p.getOriginalFilename() != null ?
                                    p.getOriginalFilename().substring(p.getOriginalFilename().lastIndexOf(".")) : ".png");
                    File photoFile;
                    try {
                        photoFile = File.createTempFile("photo_", photoFileName);
                        p.transferTo(photoFile);
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Photo save error: " + e.getMessage()));
                    }
                    return uploadToSupabase(photoFile,
                            p.getContentType() != null ? p.getContentType() : "application/octet-stream",
                            photoFileName)
                            .doFinally(signal -> photoFile.delete());
                });

        Mono<String> videoLinkMono = Mono.justOrEmpty(video)
                .filter(v -> !v.isEmpty())
                .flatMap(v -> {
                    String videoFileName = "video_" + System.currentTimeMillis() +
                            (v.getOriginalFilename() != null ?
                                    v.getOriginalFilename().substring(v.getOriginalFilename().lastIndexOf(".")) : ".mp4");
                    File videoFile;
                    try {
                        videoFile = File.createTempFile("video_", videoFileName);
                        v.transferTo(videoFile);
                    } catch (IOException e) {
                        return Mono.error(new RuntimeException("Video save error: " + e.getMessage()));
                    }
                    return uploadToSupabase(videoFile,
                            v.getContentType() != null ? v.getContentType() : "application/octet-stream",
                            videoFileName)
                            .doFinally(signal -> videoFile.delete());
                });

        return Mono.zip(photoLinkMono.defaultIfEmpty(null), videoLinkMono.defaultIfEmpty(null))
                .flatMap(tuple -> {
                    registration.setPhotoLink(tuple.getT1());
                    registration.setVideoLink(tuple.getT2());

                    // Firebase save logic
                    return Mono.fromCallable(() -> {
                        userRef.orderByChild("email").equalTo(registration.getEmail())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            response.append("User with email ").append(registration.getEmail()).append(" already exists.");
                                        } else {
                                            userRef.child(registration.getEmail().replace(".", "_")).setValueAsync(registration);
                                            response.append("User added: ").append(registration.getFullName());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {
                                        response.append("Firebase error: ").append(error.getMessage());
                                    }
                                });
                        return response.toString();
                    });
                })
                .map(result -> ResponseEntity.ok(result.isEmpty() ? "Processing... Check logs" : result))
                .onErrorResume(e -> {
                    e.printStackTrace();
                    return Mono.just(ResponseEntity.status(500).body("Error: " + e.getMessage()));
                });
    }

    private boolean storeOtp(DatabaseReference ref, String email, String otp) {
        final boolean[] stored = {false};

        DatabaseReference otpRef = ref.child("otps").child(email.replace(".", "_"));

        Map<String, Object> otpData = new HashMap<>();
        otpData.put("otp", hash(otp));
        otpData.put("expiresAt", Instant.now().plusSeconds(600).toString());
        otpData.put("used", false);

        try {
            ApiFuture<Void> future = otpRef.setValueAsync(otpData);
            future.get();
            System.out.println("OTP stored for E-Mail: " + email);
            stored[0] = true;
        } catch (Exception e) {
            System.out.println("Failed to save OTP: " + e.getMessage());
            e.printStackTrace();
        }

        return stored[0];
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestParam String email,
                                            @RequestParam String otpEntered) {
        try {
            boolean valid = checkOtp(userRef, email, otpEntered);
            if (valid) return ResponseEntity.ok("OTP verified successfully");
            else return ResponseEntity.status(400).body("Invalid or expired OTP");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error verifying OTP");
        }
    }

    private boolean checkOtp(DatabaseReference ref, String email, String otpEntered) {
        DatabaseReference otpRef = ref
                .child("otps")
                .child(email.replace(".", "_"));

        final boolean[] result = {false};
        otpRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                String storedHash = snapshot.child("otp").getValue(String.class);
                String expiresAtStr = snapshot.child("expiresAt").getValue(String.class);
                boolean used = snapshot.child("used").getValue(Boolean.class);
                Instant expiresAt = Instant.parse(expiresAtStr);

                if (!used && expiresAt.isAfter(Instant.now()) && hash(otpEntered).equals(storedHash)) {
                    result[0] = true;
                    otpRef.child("used").setValueAsync(true);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Firebase error: " + error.getMessage());
            }
        });

        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) {
        }
        return result[0];
    }
}