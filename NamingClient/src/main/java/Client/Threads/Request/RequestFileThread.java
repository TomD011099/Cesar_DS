package Client.Threads.Request;

import Client.Util.Ports;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

/**
 * This thread is created when the user wants a file
 */
public class RequestFileThread extends Thread {
    private InetAddress dest;   //The location of the requested file
    private String fileName;    //The name of the requested file
    private String dir;         //The absolute path to the download folder

    /**
     * The constructor
     *
     * @param dest     The location of the requested file
     * @param fileName The name of the requested file
     * @param dir      The directory in which the file has to be downloaded
     */
    public RequestFileThread(InetAddress dest, String fileName, String dir) {
        this.dest = dest;
        this.fileName = fileName;
        this.dir = dir;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, Ports.requestPort);

            // Get the inputstream from the socket
            InputStream in = socket.getInputStream();

            // Create a writer to write to the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Send filename
            writer.println(fileName);

            // Create an empty file
            File file = new File(dir + fileName);
            file.createNewFile();

            // Create a stream to write to the file
            OutputStream fileOut = new FileOutputStream(file);

            // Create a buffer
            byte[] buf = new byte[4096];

            // Read the data from the socket and save it in the file
            int count;
            while ((count = in.read(buf)) > 0) {
                fileOut.write(buf, 0, count);
            }

            // Close all connections
            writer.close();
            fileOut.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("RequestFile Thread ended");
    }
}
