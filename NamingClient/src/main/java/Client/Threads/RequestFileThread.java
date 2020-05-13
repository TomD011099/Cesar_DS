package Client.Threads;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class RequestFileThread extends Thread{
    private final InetAddress dest;
    private final String fileName;
    private final String dir;

    public RequestFileThread(InetAddress dest, String fileName, String dir) {
        this.dest = dest;
        this.fileName = fileName;
        this.dir = dir;
    }

    @Override
    public void run() {
        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, 12345);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            //Give command
            writer.println("File_request");

            reader.readLine();

            //Send filename
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
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(dir + fileName));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();

            writer.close();
            reader.close();
            fileOutputStream.close();
            out.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
