package av.gdhns.club.main.controllers;

import av.gdhns.club.main.model.UserRegistrationModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.Http;
import com.google.firebase.database.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@RestController
@RequestMapping("/admin")
public class AdminController {
    DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("member_registrations");
    DatabaseReference adminRef = FirebaseDatabase.getInstance().getReference("admin");

    private final String[] apiKeyValue = {System.getenv("admin0"), System.getenv("admin1")};

    @GetMapping("/registrations")
    public ResponseEntity<?> getRegistrations(@RequestHeader("X-API-KEY") String apiKey) {
        final ResponseEntity<?>[] response = new ResponseEntity<?>[1];

        if (!apiKeyValue[0].equals(apiKey) || !apiKeyValue[1].equals(apiKey)) {
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

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String userName, @RequestParam String password) {
        String passwordHash = hash(password);

        try {
            CompletableFuture<ResponseEntity<String>> future = new CompletableFuture<>();

            adminRef.child(userName).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        future.complete(ResponseEntity.status(HttpStatus.NOT_FOUND).body("User does not exist"));
                        return;
                    }

                    String storedHash = snapshot.child("password").getValue(String.class);

                    if (storedHash.equals("null") || storedHash.isEmpty()) {
                        adminRef.child(userName).child("password").setValue(passwordHash, (error, ref) -> {
                            if (error == null)
                                future.complete(ResponseEntity.ok("Password created"));
                            else
                                future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body("Error setting password"));
                        });
                        return;
                    }

                    if (storedHash.equals(passwordHash)) {
                        String apiKey = System.getenv(userName);
                        future.complete(ResponseEntity.ok(apiKey != null ? apiKey : "API key not found"));
                    } else {
                        future.complete(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid Password"));
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    future.complete(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Firebase error: " + error.getMessage()));
                }
            });

            return future.get();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }


    public static String hash(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
