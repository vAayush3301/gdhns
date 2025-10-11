package av.gdhns.club.main.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public static void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String firebaseJson = System.getenv("GOOGLE_APPLICATION_CREDENTIALS_JSON");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(firebaseJson.getBytes())))
                    .setDatabaseUrl("https://ggpsclub-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("Firebase Initialization Successful");
        }
    }
}
