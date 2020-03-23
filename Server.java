import java.io.IOException;
import java.net.*;

public class Server {
    public static void main(String[] args) {
        try {
            Bank bank = new Bank();
            ServerSocket serverSocket = new ServerSocket(5000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
