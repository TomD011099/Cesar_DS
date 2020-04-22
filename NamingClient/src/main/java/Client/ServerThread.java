package Client;

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
    private final ServerSocket serverSocket;
    private final Client client;

    public ServerThread(int port, Client client) throws IOException {
        serverSocket = new ServerSocket(port);
        this.client = client;
    }

    public void run() {
        try {
            while (true) {
                Socket socket = serverSocket.accept();

                // Create a reader to read from the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String read = reader.readLine();
                String[] parsed = read.split(" ");
                if (parsed[0].equals("prev")) {
                    client.setPrevNode(InetAddress.getByName(parsed[1].substring(1)));
                    System.out.println("prevNode updated to: " + client.getPrevNode());
                } else if (parsed[0].equals("next")) {
                    client.setNextNode(InetAddress.getByName(parsed[1].substring(1)));
                    System.out.println("nextNode updated to: " + client.getNextNode());
                }

                // Close all connections
                socket.close();
            }
        } catch (IOException ioException) {
            client.failure();
        }
    }
}
