package Client.Threads.Request;

import java.io.*;
import java.net.Socket;

/**
 * A thread for send requested files
 */
public class SendRequestedFileThread extends Thread {
    private Socket socket;  //The socket created by serverSocket
    private String dir;     //The absolute path to the replication directory

    /**
     * Constructor
     *
     * @param socket The socket created by the ServerSocket
     * @param dir    The absolute path to replicaDir
     */
    public SendRequestedFileThread(Socket socket, String dir) {
        this.socket = socket;
        this.dir = dir;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            // Get the outputstream from the socket
            OutputStream out = socket.getOutputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Read the name of the requested file
            String fileName = reader.readLine();

            // Create a buffer and a stream to read from the file
            File file = new File(dir + fileName);
            byte[] buf = new byte[4096];
            InputStream fileInputStream = new FileInputStream(file);

            // Send the contents of the file
            int count;
            while ((count = fileInputStream.read(buf)) > 0) {
                out.write(buf, 0, count);
            }

            // Close all connections
            reader.close();
            fileInputStream.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println("SendRequestedFile Thread ended");
    }
}
