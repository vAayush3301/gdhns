package av.gdhns.club.main.controllers;

import av.gdhns.club.main.model.UserRegistrationModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.Collections;

@RestController
@RequestMapping("/registration_member")
public class UserController {
    private final String driveJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_DRIVE_API_JSON");

    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("member_registrations");

    @PostMapping
    public ResponseEntity<String> createMemberRegistration(@RequestParam("data") String userData, @RequestParam("photo")MultipartFile photo, @RequestParam("video") MultipartFile video) {
        try {
            UserRegistrationModel registration = new ObjectMapper().readValue(userData, UserRegistrationModel.class);
            registration.setCreatedAt(java.time.Instant.now().toString());

            java.io.File photoFile = java.io.File.createTempFile("photo", photo.getOriginalFilename());
            photo.transferTo(photoFile);
            String photoLink = uploadToDrive(photoFile, photo.getContentType());

            java.io.File videoFile = java.io.File.createTempFile("video", video.getOriginalFilename());
            video.transferTo(videoFile);
            String videoLink = uploadToDrive(videoFile, video.getContentType());

            registration.setPhotoLink(photoLink);
            registration.setVideoLink(videoLink);

            System.out.println("Pushing registration: " + registration);
            userRef.child(registration.getPhoneNumber()).push().setValueAsync(registration).get();

            return ResponseEntity.ok("New Member Registered: " + registration.getFullName());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    public Drive getDriveService() throws Exception {
        GoogleCredentials credentials = GoogleCredentials.fromStream(new ByteArrayInputStream(driveJson.getBytes()))
                .createScoped(Collections.singleton(DriveScopes.DRIVE_FILE));

        return new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName("GGPS Club Project").build();
    }

    public String uploadToDrive(java.io.File localFile, String mimeType) throws Exception {
        Drive service = getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(localFile.getName());

        FileContent mediaContent = new FileContent(mimeType, localFile);

        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, webViewLink")
                .execute();

        return uploadedFile.getWebViewLink();
    }
}
