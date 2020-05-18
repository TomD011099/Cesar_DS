package Client.Threads.Request;

import java.io.*;
import java.net.Socket;

public class SendRequestedFileThread extends Thread {
    private Socket socket;
    private String dir;

    public SendRequestedFileThread(Socket socket, String dir) {
        this.socket = socket;
        this.dir = dir;
    }

    @Override
    public void run() {
        try {
            // Get the in- and outputstreams from the socket
            OutputStream out = socket.getOutputStream();

            // Create a writer to write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String fileName = reader.readLine();

            // Make an array of bytes and store the file in said array
            File file = new File(dir + fileName);
            byte[] buf = new byte[4096];
            InputStream fileInputStream = new FileInputStream(file);

            int count;
            while ((count = fileInputStream.read(buf)) > 0) {
                out.write(buf, 0, count);
            }

            reader.close();
            fileInputStream.close();
            out.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
