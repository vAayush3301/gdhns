package av.gdhns.club.main.controllers;

import av.gdhns.club.main.model.UserRegistrationModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.Value;
import com.google.firebase.database.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/admin")
public class AdminController {
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("member_registrations");

    private final String apiKeyValue = System.getenv("API_KEY");

    @GetMapping("/registrations")
    public ResponseEntity<?> getRegistrations(@RequestHeader("X-API-KEY") String apiKey) {
        final ResponseEntity<?>[] response = new ResponseEntity<?>[1];

        if (!apiKeyValue.equals(apiKey)) {
            System.out.println("Actual Key: " + apiKeyValue);
            System.out.println("Entered Key: " + apiKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API Key");
        }

        try {
            List<UserRegistrationModel> registrations = getAllUsersSync();
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(registrations);
            response[0] = ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json);
        } catch (Exception e) {
            e.printStackTrace();
            response[0] = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

        return response[0];
    }

    public List<UserRegistrationModel> getAllUsersSync() throws InterruptedException {
        List<UserRegistrationModel> registrations = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot child : snapshot.getChildren()) {
                    String key = child.getKey();
                    if (key.equals("otps")) continue;

                    UserRegistrationModel registration = child.getValue(UserRegistrationModel.class);
                    if (registration != null) registrations.add(registration);
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Firebase fetch error: " + error.getMessage());
                latch.countDown();
            }
        });

        latch.await();
        return registrations;
    }
}
