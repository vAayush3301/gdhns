package av.gdhns.arr;

import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {
    private final Firestore firestore;

    public DatabaseService(@Qualifier("ARR") FirebaseApp arrApp) {
        this.firestore = FirestoreClient.getFirestore(arrApp);
    }
}
