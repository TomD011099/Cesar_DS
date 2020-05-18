package Client.Threads.Replicate;

import Client.Util.Ports;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class SendReplicateFileThread extends Thread {
    private InetAddress dest;
    private String dir;
    private String fileName;

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

    @Override
    public void run() {
        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, Ports.replicatePort);

            // Get the outputstream from the socket
            OutputStream out = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(out, true);

            writer.println(fileName);

            // Make an array of bytes and store the file in said array
            File file = new File(dir + fileName);
            byte[] buf = new byte[4096];
            InputStream fileInputStream = new FileInputStream(file);

            int count;
            while ((count = fileInputStream.read(buf)) > 0) {
                out.write(buf, 0, count);
                System.out.println("Send:\n" + hex(buf));
            }

            writer.close();
            fileInputStream.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            if (b != 0x0)
                out.append((char) b).append(String.format(" 0x%02X\n", b));
        }

        return out.toString();
    }
}