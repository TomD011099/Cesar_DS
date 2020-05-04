package Client;

import java.io.*;
import java.net.*;

public class ServerThread implements Runnable {
    private final ServerSocket serverSocket;
    private final Client client;
    private volatile boolean stop;

    public ServerThread(int port, Client client) throws IOException {
        serverSocket = new ServerSocket(port);
        this.client = client;
        this.stop = false;
    }

    public void run() {
        try {
            while (!stop) {
                Socket socket = serverSocket.accept();

                // Create a reader to read from the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String command = reader.readLine();
                String[] array = command.split(":");

                switch (array[0]) {
                    case "Update":
                        String in = reader.readLine();
                        String[] parsed = in.split(" ");
                        if (parsed[0].equals("prev")) {
                            client.setPrevNode(InetAddress.getByName(parsed[1].substring(1)));
                            System.out.println("prevNode updated to: " + client.getPrevNode());
                        } else if (parsed[0].equals("next")) {
                            client.setNextNode(InetAddress.getByName(parsed[1].substring(1)));
                            System.out.println("nextNode updated to: " + client.getNextNode());
                        }
                        break;
                    case "File_replicate":
                        client.getFileTransfer().receiveReplication(socket);
                        break;
                    case "Shutdown":
                        client.ownerShutdown(array[1]);
                        break;
                    case "File_request":
                        client.getFileTransfer().sendRequestedFile(socket);
                }

                // Close all connections
                socket.close();
            }
        } catch (IOException ioException) {
            client.failure();
        }
    }

    public void stop() {
        stop = true;
    }
}
