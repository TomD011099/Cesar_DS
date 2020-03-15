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

public class Client {
    public static void main(String[] args) {
        // Test for correct amount of inputs
        if (args.length != 4) return;

        // Load inputs to variables
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String hostPath = args[2];
        String localPath = args[3];

        try {
            // Create a socket to communicate
            Socket socket = new Socket(host, port);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader and writer to read from and write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            // Create an outputstream to write files to the socket
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(localPath));

            // Send the path of the file to be copied to the server
            writer.println(hostPath);

            // Read the size of the requested file the server sent back and create a byte array of that size
            int len = Integer.parseInt(reader.readLine());
            byte[] bytes = new byte[len];
            System.out.println("Total amount of bytes to read: " + bytes.length);

            int bytesRead;
            int current = 0;

            // Read the file from the socket
            do {
                //TODO: with multiple reads without stopping Server, gets stuck here. Maybe fixed
                bytesRead = in.read(bytes, current, (bytes.length - current));
                current += bytesRead;
                System.out.println("Read " + current + " bytes, Estimated " + in.available() + " bytes left");
            } while (current < len || bytesRead > 0);
            System.out.println("File received, downloading to " + localPath);

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();
            System.out.println("File " + hostPath + " downloaded to " + localPath + " (" + bytes.length + ")");

            // Let the server know you're done
            writer.println("Done.");

            // Close all connections
            fileOutputStream.close();
            in.close();
            reader.close();
            out.close();
            writer.close();
            socket.close();

        } catch (UnknownHostException e) {
            System.err.println("Server not found at: " + host + ":" + port);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
