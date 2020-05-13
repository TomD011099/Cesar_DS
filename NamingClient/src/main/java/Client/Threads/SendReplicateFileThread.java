package Client.Threads;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SendReplicateFileThread extends Thread {
    private final InetAddress dest;
    private final String dir;
    private final String fileName;

    /**
     * Constructor
     *
     * @param dest Ip of dest
     * @param dir Directory to copy from
     * @param fileName File to copy
     */
    public SendReplicateFileThread(InetAddress dest, String dir, String fileName) {
        this.dest = dest;
        this.dir = dir;
        this.fileName = fileName;
    }

    @Override
    public void run() {
        try {
            System.out.println("Replication for " + fileName + " started.");

            // Create a socket to communicate
            Socket socket = new Socket(dest, 12345);

            // Get the outputstream from the socket
            OutputStream out = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(out, true);

            writer.println("File_replicate");
            writer.println(fileName);

            // Make an array of bytes and store the file in said array
            File file = new File(dir + fileName);
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            fileInputStream.read(bytes, 0, bytes.length);

            // Let the client know how much bytes will be sent
            writer.println(bytes.length);

            // Send the bytes
            out.write(bytes);
            out.flush();

            writer.close();
            fileInputStream.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
