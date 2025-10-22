package av.gdhns.club.main.controllers;

import av.gdhns.club.main.helpers.Mail;
import av.gdhns.club.main.model.UserRegistrationModel;
import com.google.api.core.ApiFuture;
import com.google.firebase.database.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static av.gdhns.club.main.helpers.Mail.hash;

@RestController
@RequestMapping("/registration_member")
public class UserController {
    private final DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("member_registrations");

    @PostMapping
    public Mono<ResponseEntity<String>> createMemberRegistration(@RequestBody UserRegistrationModel registration) {
        if (registration == null || registration.getEmail() == null || registration.getEmail().isEmpty()) {
            return Mono.just(ResponseEntity.status(400).body("Error: Email is required"));
        }
        registration.setCreatedAt(Instant.now().toString());

        return upload(registration);
    }

    private Mono<ResponseEntity<String>> upload(UserRegistrationModel registration) {
        final StringBuilder response = new StringBuilder();

        return Mono.fromCallable(() -> {
                    userRef.orderByChild("email").equalTo(registration.getEmail())
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        response.append("User with email ").append(registration.getEmail()).append(" already exists.");
                                    } else {
                                        userRef.child(registration.getEmail().replace(".", "_"))
                                                .setValueAsync(registration);
                                        response.append("User added: ").append(registration.getFullName());
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    response.append("Firebase error: ").append(error.getMessage());
                                }
                            });

                    return response.toString();
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
            if (valid) {
                userRef.child("otps").child(email.replace("@", "_")).removeValueAsync();

                return ResponseEntity.ok("OTP verified successfully");
            } else return ResponseEntity.status(400).body("Invalid or expired OTP");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error verifying OTP");
        }
    }

    @PostMapping("/send")
    public Mono<ResponseEntity<String>> sendOtp(@RequestParam String email) {
        try {
            String otp = Mail.generateOTP();

            boolean sent = Mail.sendOTP(email, otp);
            if (!sent) {
                return Mono.just(ResponseEntity.status(500).body("Unable to send OTP!!!"));
            }

            boolean stored = storeOtp(userRef, email, otp);
            if (!stored) {
                return Mono.just(ResponseEntity.status(500).body("Unable to store OTP!!!"));
            }

            return Mono.just(ResponseEntity.status(200).body("OTP sent and stored!!!"));
        } catch (Exception e) {
            e.printStackTrace();
            return Mono.just(ResponseEntity.status(500).body("Unable to send OTP!!! Internal Server Error: " + e.getMessage()));
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