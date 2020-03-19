import java.io.*;
import java.net.Socket;

public class ServerThread extends Thread {
    protected Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        byte[] bytes;

        try {
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
                System.err.println(fileName + " does not exist.");
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

        }  catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
