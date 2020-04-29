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

                switch (command) {
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
                    case "File_request":
                        client.getFileTransfer().sendRequestedFile(socket);
                        break;

                    case "Delete_file":
                        String filename = reader.readLine();
                        String logFilename = "Log_" + filename + ".txt";
                        File file = new File("./remote/" + filename);
                        File logFile = new File("./remote/" + logFilename);
                        deleteFile(file);
                        deleteFile(logFile);
                        break;
                }

                // Close all connections
                socket.close();
            }
        } catch (IOException ioException) {
            client.failure();
        }
    }

    private void deleteFile(File file) {
        if (file.delete())
            System.out.println(file.getName() + " deleted successfully!");
        else
            System.out.println("Could not delete file: " + file.getName());
    }

    public void stop() {
        stop = true;
    }
}
