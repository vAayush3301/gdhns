package av.gdhns.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp main_init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String firebaseJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON_MAIN");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseJson.getBytes())))
                    .setDatabaseUrl("https://ggpsclub-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .build();

            System.out.println("Firebase Main Initialization");
            return FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public FirebaseApp arrangement_init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String firebaseJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON_ARRANGEMENT");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseJson.getBytes())))
                    .setDatabaseUrl("https://ggpsclub-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .build();

            System.out.println("Firebase Arrangement Initialization");
            return FirebaseApp.initializeApp(options, "ARR");
        }
        return FirebaseApp.getInstance();
    }
}
