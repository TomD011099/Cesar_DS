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
           |  --------------------->  |
           |                          |
           |            ACK           |
           |  <---------------------  |
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

            byte[] bytes;

            while (true) {
                // Create a socket to talk to a client
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                System.out.println("--------------------");

                // Get the in- and outputstreams from the socket
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();

                // Create a reader and writer to read from and write to the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                PrintWriter writer = new PrintWriter(out, true);

                // Read the path to the file that has to be copied
                String fileName = reader.readLine();
                System.out.println("File " + fileName + " requested.");

                // Check if the file exists
                File file = new File(fileName);
                if (!file.exists()) {
                    System.err.println(fileName + "does not exist.");
                    return;
                } else
                    System.out.println("File " + fileName + " found, starting transfer.");

                // Make an array of bytes and store the file in said array
                bytes = new byte[(int) file.length()];
                BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
                fileInputStream.read(bytes, 0, bytes.length);

                // Let the client know how much bytes will be sent
                writer.println(bytes.length);

                // Send the bytes
                System.out.println("Sending " + fileName + " (" + bytes.length + " bytes)");
                out.write(bytes);
                out.flush();

                // Check if the client has sent the ACK
                if (reader.readLine().equals("Done."))
                    System.out.println("Done.\n");

                // Close all connections
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
