package Client;

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
    private ServerSocket serverSocket;
    private Socket socket;
    private String read = "null";

    public ServerThread(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public String getRead() {
        return read;
    }

    public void run() {
        try {
            while (true) {
                socket = serverSocket.accept();

                // Create a reader to read from the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                read = reader.readLine();
                System.out.println("Message received: " + read);

                // Close all connections
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
