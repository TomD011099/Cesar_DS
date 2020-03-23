import java.io.*;
import java.net.*;


/*
    Communication setup:

        Client                      Server
           |         filePath         |
           |  --------------------->  |
           |                          |
           |         fileSize         |
           |  <---------------------  |
           |                          |
           |           file           |
           |  <---------------------  |
           |                          |
           |            ACK           |
           |  --------------------->  |
           |                          |
 */

public class Server {
    public static void main(String[] args) {
        // Test for correct amount of inputs
        if (args.length != 1) return;

        // Parse the portnumber
        int port = Integer.parseInt(args[0]);

        try {
            // Create the serversocket
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server online on port " + port + "\n");

            while (true) {
                // Create a socket to talk to a client
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket);
                Thread t = new ServerThread(socket);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
