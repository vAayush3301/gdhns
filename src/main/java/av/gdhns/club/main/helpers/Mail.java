package av.gdhns.club.main.helpers;

import okhttp3.*;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;

public class Mail {
    private static final String API_KEY = "7851eb09f53f5fdf1a606d6aab50872dc87770c332f14dde88730ff24d737c18";
    private static final String FROM = "ggpsdhn@16481b8a47cfd701.maileroo.org";
    private static String ENDPOINT = "https://smtp.maileroo.com/api/v2/emails";

    public static String generateOTP() {
        SecureRandom rnd = new SecureRandom();
        int num = 100000 + rnd.nextInt(900000);
        return String.valueOf(num);
    }

    public static String hash(String otp) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(otp.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static boolean sendOTP(String to, String otp) throws IOException {
        OkHttpClient client = new OkHttpClient();

        JSONObject body = new JSONObject();
        body.put("from", FROM);
        body.put("to", to);
        body.put("subject", "Your Verification OTP");
        body.put("text", "Your OTP is: " + otp + " — valid for 5 minutes.");

        Request req = new Request.Builder()
                .url(ENDPOINT)
                .addHeader("Authorization", "Bearer " + API_KEY)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(body.toString(), MediaType.get("application/json")))
                .build();

        try (Response resp = client.newCall(req).execute()) {
            if (resp.isSuccessful()) {
                System.out.println("Email sent!");
                return true;
            } else {
                System.err.println("Error: " + resp.code() + " — " + resp.body().string());
                return false;
            }
        }
    }
}
