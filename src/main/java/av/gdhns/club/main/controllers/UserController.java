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

    @PostMapping
    public ResponseEntity<String> createMemberRegistration(@RequestParam("data") String userJson, @RequestParam("photo") MultipartFile photo, @RequestParam("video") MultipartFile video) {
        if (userJson == null || userJson.isEmpty()) {
            return ResponseEntity.status(400).body("Error: userJson is required");
        }
        if (photo == null || photo.isEmpty()) {
            return ResponseEntity.status(400).body("Error: Photo is required");
        }
        if (video == null || video.isEmpty()) {
            return ResponseEntity.status(400).body("Error: Video is required");
        }

        final StringBuilder response = new StringBuilder();

        try {
            UserRegistrationModel registration = new ObjectMapper().readValue(userJson, UserRegistrationModel.class);
            registration.setCreatedAt(Instant.now().toString());

            if (!photo.isEmpty() || !video.isEmpty()) {
                File photoFile = File.createTempFile("photo_", "_" + photo.getOriginalFilename());
                photo.transferTo(photoFile);

                File videoFile = File.createTempFile("video_", "_" + video.getOriginalFilename());
                video.transferTo(videoFile);

                String photoLink = uploadToSupabase(photoFile, photo.getContentType());
                String videoLink = uploadToSupabase(videoFile, video.getContentType());

                registration.setPhotoLink(photoLink);
                registration.setVideoLink(videoLink);
            }

            userRef.orderByChild("email").equalTo(registration.getEmail())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                response.append("User with email ").append(registration.getEmail()).append(" already exists.");
                            } else {
                                userRef.child(registration.getEmail()).push().setValueAsync(registration);
                                response.append("User added: ").append(registration.getFullName());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            response.append("Firebase error: ").append(databaseError.getMessage());
                        }
                    });

            System.out.println("Pushing registration: " + registration);
            userRef.child(registration.getPhoneNumber()).push().setValueAsync(registration).get();

            return ResponseEntity.ok("Processing... Check logs for result");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public String uploadToSupabase(File file, String contentType) throws IOException {
        String fileName = file.getName();
        System.out.println("Uploading File: " + fileName);

        WebClient client = WebClient.builder()
                .baseUrl(SUPABASE_URL)
                .defaultHeader("apikey", SUPABASE_KEY)
                .defaultHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .build();

        byte[] fileBytes = Files.readAllBytes(file.toPath());

        return client.put()
                .uri("/storage/v1/object/" + SUPABASE_STORAGE_BUCKET + "/" + fileName)
                .contentType(MediaType.parseMediaType(contentType))
                .bodyValue(fileBytes)
                .retrieve()
                .onStatus(status -> status.isError(), response -> {
                    System.out.println("Supabase error: " + response.statusCode() + " - " + response.bodyToMono(String.class).block());
                    return response.createException().flatMap(Mono::error);
                })
                .bodyToMono(String.class)
                .block();
    }

    public String getPublicUrl(String fileName) {
        return SUPABASE_URL + "/storage/v1/object/public/" + SUPABASE_STORAGE_BUCKET + "/" + fileName;
    }
}
