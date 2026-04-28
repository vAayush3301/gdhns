package av.gdhns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.time.Instant;

@SpringBootApplication
public class MainApplication {
    public static final Instant START_TIME = Instant.now();

    public static void main(String[] args) throws IOException {
        SpringApplication.run(MainApplication.class, args);
    }

}
