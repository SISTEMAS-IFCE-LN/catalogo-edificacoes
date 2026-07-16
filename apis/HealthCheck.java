import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URI;

public class HealthCheck {
    public static void main(String[] args) {
        try {
            URL url = URI.create("http://localhost:8080/actuator/health").toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            System.exit(conn.getResponseCode() < 400 ? 0 : 1);
        } catch (Exception e) {
            System.exit(1);
        }
    }
}