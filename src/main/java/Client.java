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

        Socket socket = null;
        InputStream in = null;
        OutputStream out = null;
        BufferedReader reader = null;
        PrintWriter writer = null;
        BufferedOutputStream fileOutputStream = null;

        try {
            // Create a socket to communicate
            socket = new Socket(host, port);

            // Get the in- and outputstreams from the socket
            in = socket.getInputStream();
            out = socket.getOutputStream();

            // Create a reader and writer to read from and write to the socket
            reader = new BufferedReader(new InputStreamReader(in));
            writer = new PrintWriter(out, true);

            // Create an outputstream to write files to the socket
            fileOutputStream = new BufferedOutputStream(new FileOutputStream(localPath));

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
                //TODO: with multiple reads without stopping Server, gets stuck here. Possible that in not ended correctly
                // Never returns -1
                bytesRead = in.read(bytes, current, (bytes.length - current));
                current += bytesRead;
                if (bytesRead != -1)
                    System.out.println("Read " + current + " bytes, Estimated " + in.available() + " bytes left");
                System.out.println("\tCurrent = " + current + "\n\tbytesRead = " + bytesRead + "\n\tLength bytes = " + bytes.length);
            } while (bytesRead > 0 && current < len);
            System.out.println("File received, downloading to " + localPath);

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();
            System.out.println("File " + hostPath + " downloaded to " + localPath + " (" + bytes.length + ")");

            // Let the server know you're done
            writer.println("Done.");

        } catch (UnknownHostException e) {
            System.err.println("Server not found at: " + host + ":" + port);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    // Close all connections
                    in.close();
                    reader.close();
                    out.flush();
                    out.close();
                    writer.flush();
                    writer.close();
                    socket.close();

                    System.out.println("Connections closed");
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }

            }
        }
    }
}
