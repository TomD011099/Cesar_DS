package Client.Threads;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;

public class ReceiveReplicateFileThread extends Thread {
    private final Socket socket;
    private final String dir;
    private HashSet<String> localFileSet;
    private InetAddress prevNode;

    public ReceiveReplicateFileThread(Socket socket, String dir, HashSet<String> localFileSet, InetAddress prevNode) {
        System.out.println("\nSocket in thread constr-var" + socket.toString() + "\nChannel: " + socket.getChannel() + "\nIs closed: " + socket.isClosed());
        this.socket = socket;
        System.out.println("\nSocket in thread constr-field" + this.socket.toString() + "\nChannel: " + this.socket.getChannel() + "\nIs closed: " + this.socket.isClosed());
        this.dir = dir;
        this.localFileSet = localFileSet;
        this.prevNode = prevNode;
    }

    @Override
    public void run() {
        try {
            System.out.println("\nSocket in thread " + socket.toString() + "\nChannel: " + socket.getChannel() + "\nIs closed: " + socket.isClosed());

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            writer.println("OK");

            String fileName = reader.readLine();

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

            if (localFileSet.contains(fileName) || (fileName.startsWith("log_") && localFileSet.contains(fileName.substring(4, fileName.length()-8)))) {
                Thread sendReplicateFileThread = new SendReplicateFileThread(prevNode, dir, fileName);
                sendReplicateFileThread.start();
                File file = new File(dir + fileName);
                file.delete();
            }

            writer.close();
            reader.close();
            fileOutputStream.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
