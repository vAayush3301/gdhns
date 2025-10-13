package av.gdhns.club.main.controllers;

import av.gdhns.club.main.model.UserRegistrationModel;
import com.fasterxml.jackson.databind.ObjectMapper;
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
            @RequestParam("photo") MultipartFile photo,
            @RequestParam("video") MultipartFile video) {
        final StringBuilder response = new StringBuilder();

        // Validate inputs
        if (userJson == null || userJson.isEmpty()) {
            return Mono.just(ResponseEntity.status(400).body("Error: userJson is required"));
        }
        if (photo == null || photo.isEmpty()) {
            return Mono.just(ResponseEntity.status(400).body("Error: Photo is required"));
        }
        if (video == null || video.isEmpty()) {
            return Mono.just(ResponseEntity.status(400).body("Error: Video is required"));
        }

        try {
            // Parse JSON
            UserRegistrationModel registration = new ObjectMapper().readValue(userJson, UserRegistrationModel.class);
            registration.setCreatedAt(Instant.now().toString());

            // Prepare file names
            String photoFileName = "photo_" + System.currentTimeMillis() +
                    (photo.getOriginalFilename() != null ? photo.getOriginalFilename().substring(photo.getOriginalFilename().lastIndexOf(".")) : ".png");
            String videoFileName = "video_" + System.currentTimeMillis() +
                    (video.getOriginalFilename() != null ? video.getOriginalFilename().substring(video.getOriginalFilename().lastIndexOf(".")) : ".mp4");

            // Create temporary files
            File photoFile = File.createTempFile("photo_", photoFileName);
            File videoFile = File.createTempFile("video_", videoFileName);

            // Transfer files and upload to Supabase reactively
            return Mono.just(photo)
                    .flatMap(p -> Mono.fromCallable(() -> {
                        p.transferTo(photoFile);
                        return photoFile;
                    }))
                    .flatMap(pFile -> uploadToSupabase(pFile, photo.getContentType() != null ? photo.getContentType() : "application/octet-stream", photoFileName))
                    .flatMap(photoLink -> Mono.just(video)
                            .flatMap(v -> Mono.fromCallable(() -> {
                                v.transferTo(videoFile);
                                return videoFile;
                            }))
                            .flatMap(vFile -> uploadToSupabase(vFile, video.getContentType() != null ? video.getContentType() : "application/octet-stream", videoFileName))
                            .map(videoLink -> {
                                registration.setPhotoLink(photoLink);
                                registration.setVideoLink(videoLink);
                                return registration;
                            }))
                    .flatMap(reg -> Mono.fromCallable(() -> {
                        // Check for existing user by email
                        userRef.orderByChild("email").equalTo(reg.getEmail())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            response.append("User with email ").append(reg.getEmail()).append(" already exists.");
                                        } else {
                                            userRef.child(reg.getEmail()).push().setValueAsync(reg);
                                            response.append("User added: ").append(reg.getFullName());
                                        }
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                        response.append("Firebase error: ").append(databaseError.getMessage());
                                    }
                                });

                        // Save by phone number (consider consolidating)
                        userRef.child(reg.getPhoneNumber()).push().setValueAsync(reg);
                        return response.toString();
                    }))
                    .map(result -> ResponseEntity.ok(result.isEmpty() ? "Processing... Check logs for result" : result))
                    .doFinally(signal -> {
                        // Clean up temporary files
                        if (photoFile.exists()) photoFile.delete();
                        if (videoFile.exists()) videoFile.delete();
                    })
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.just(ResponseEntity.status(500).body("Error: " + e.getMessage()));
                    });
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
}