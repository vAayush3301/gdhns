package av.gdhns.club.main.controllers;

import av.gdhns.club.main.model.UserRegistrationModel;
import com.google.firebase.database.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registration_member")
public class UserController {
    private final String driveJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_DRIVE_API_JSON");

    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("member_registrations");

    @PostMapping
    public ResponseEntity<String> createMemberRegistration(@RequestBody UserRegistrationModel registration) {
        final StringBuilder response = new StringBuilder();

        try {
            userRef.orderByChild("phoneNumber").equalTo(registration.getPhoneNumber())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                response.append("User with phone ").append(registration.getPhoneNumber()).append(" already exists.");
                            } else {
                                userRef.child(registration.getPhoneNumber()).push().setValueAsync(registration);
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
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
