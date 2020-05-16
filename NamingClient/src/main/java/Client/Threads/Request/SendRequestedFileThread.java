package Client.Threads.Request;

import java.io.*;
import java.net.Socket;

public class SendRequestedFileThread extends Thread{
    private  Socket socket;
    private  String dir;

    public SendRequestedFileThread(Socket socket, String dir) {
        this.socket = socket;
        this.dir = dir;
    }

    @Override
    public void run() {
        try {
            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a writer to write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            String fileName = reader.readLine();

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
            reader.close();
            fileInputStream.close();
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
