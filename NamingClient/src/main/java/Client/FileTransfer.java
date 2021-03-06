package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FileTransfer {
    private final String localDir;
    private final String replicaDir;
    private final String requestDir;

    public FileTransfer(String localDir, String replicaDir, String requestDir) {
         this.localDir = localDir;
         this.replicaDir = replicaDir;
         this.requestDir = requestDir;
    }

    public void sendFile(Socket socket, boolean local) {
        String path;

        try {
            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a writer to write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            if (local) {
                path = localDir;
            }
            else {
                path = replicaDir;
            }

            String fileName = reader.readLine();

            // Make an array of bytes and store the file in said array
            File file = new File(path + fileName);
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            fileInputStream.read(bytes, 0, bytes.length);

            // Let the client know how much bytes will be sent
            writer.println(bytes.length);

            // Send the bytes
            out.write(bytes);
            out.flush();

            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void receiveFile(InetAddress dest, boolean request, String fileName) {
        String path;

        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, 12345);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            if (request) {
                path = requestDir;
                writer.println("File_request");
            }
            else {
                path = replicaDir;
                writer.println("File_replicate");
            }

            writer.println(fileName);

            // Read the size of the requested file the server sent back and create a byte array of that size
            int len = Integer.parseInt(reader.readLine());
            byte[] bytes = new byte[len];

            int bytesRead;
            int current = 0;

            // Read the file from the socket
            do {
                bytesRead = in.read(bytes, current, (bytes.length - current));
                current += bytesRead;
            } while (bytesRead > 0 && current < len);

            // Create an outputstream to write files to the socket
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(path + fileName));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
