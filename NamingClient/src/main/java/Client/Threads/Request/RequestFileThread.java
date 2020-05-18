package Client.Threads.Request;

import Client.Util.Ports;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class RequestFileThread extends Thread {
    private InetAddress dest;
    private String fileName;
    private String dir;

    public RequestFileThread(InetAddress dest, String fileName, String dir) {
        this.dest = dest;
        this.fileName = fileName;
        this.dir = dir;
    }

    @Override
    public void run() {
        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, Ports.requestPort);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();

            // Create a reader to read from the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            //Send filename
            writer.println(fileName);

            File file = new File(dir + fileName);
            file.createNewFile();

            OutputStream fileOut = new FileOutputStream(file);

            // Create a buffer
            byte[] buf = new byte[4096];

            int count;
            while ((count = in.read(buf)) > 0) {
                fileOut.write(buf, 0, count);
            }

            writer.close();
            fileOut.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
