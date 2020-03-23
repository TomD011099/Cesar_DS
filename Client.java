import java.io.IOException;
import java.net.*;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost", 5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
