package av.gdhns.club.main.controllers;

import av.gdhns.club.main.model.UserRegistrationModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registration_member")
public class UserController {
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("member_registrations");

    @PostMapping
    public ResponseEntity<String> createMemberRegistration(@RequestBody UserRegistrationModel registration) {
        try {
            System.out.println("Pushing registration: " + registration);
            userRef.child(registration.getPhoneNumber()).push().setValueAsync(registration).get();
            return ResponseEntity.ok("New Member Registered: " + registration.getFullName());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}
