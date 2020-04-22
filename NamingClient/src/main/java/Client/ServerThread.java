package Client;

import java.io.*;
import java.net.*;

public class ServerThread extends Thread {
    private ServerSocket serverSocket;
    private Socket socket;
    private String read = "null";
    private Client client;

    public ServerThread(int port, Client client) throws IOException {
        serverSocket = new ServerSocket(port);
        this.client = client;
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
            ioException.printStackTrace();
        }
    }
}
