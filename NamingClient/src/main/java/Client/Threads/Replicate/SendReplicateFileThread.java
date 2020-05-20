package Client.Threads.Replicate;

import Client.Util.Ports;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * A thread for replicating files
 */
public class SendReplicateFileThread extends Thread {
    private InetAddress dest;   //The destination to where the file needs to be replicated
    private String dir;         //The directory where the file to be replicated is stored
    private String fileName;    //The name of the file

    /**
     * Constructor
     *
     * @param dest     Ip of dest
     * @param dir      Directory to copy from
     * @param fileName File to copy
     */
    public SendReplicateFileThread(InetAddress dest, String dir, String fileName) {
        this.dest = dest;
        this.dir = dir;
        this.fileName = fileName;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " - Sending file...");
            System.out.println(Thread.currentThread().getName() + " - " + fileName);

            // Create a socket to communicate
            Socket socket = new Socket(dest, Ports.replicatePort);

            // Get the outputstream from the socket
            OutputStream out = socket.getOutputStream();

            // Create a writer to write on the socket
            PrintWriter writer = new PrintWriter(out, true);

            // Send the name of the file that'll be sent soon
            writer.println(fileName);

            // Create a buffer and a stream to read the raw bytes of the file
            File file = new File(dir + fileName);
            byte[] buf = new byte[4096];
            InputStream fileInputStream = new FileInputStream(file);

            // Read the bytes from the file and send them away
            int count;
            while ((count = fileInputStream.read(buf)) > 0) {
                out.write(buf, 0, count);
                //System.out.println("Send:\n" + hex(buf)); //Used for debugging
            }

            // CLose all connections
            writer.close();
            fileInputStream.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Used for debugging the streams
     *
     * @param bytes the buffer
     * @return A string of hex and ASCII, the contents of the buffer
     */
    private String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            if (b != 0x0)
                out.append((char) b).append(String.format(" 0x%02X\n", b));
        }

        return out.toString();
    }
}