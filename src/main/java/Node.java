import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Node {
    private String localDir;
    private String replicaDir;
    private String requestDir;
    private InetAddress ip;
    private InetAddress prevNode;
    private InetAddress nextNode;
    private String name;
    private InetAddress serverIp;

    public Node(String localDir, String replicaDir, String requestDir, String name) {
        try {
            serverIp = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        this.name = name;
        this.localDir = localDir;
        this.replicaDir = replicaDir;
        this.requestDir = requestDir;
    }

    private void discovery() {

    }

    private void bootstrap() {
        // TODO ask server where to put files
    }

    private void shutdown() {
        // TODO let others know to change their prevNode or nextNode
        // TODO relocate hosted files that were on the node
    }

    private void failure() {


        shutdown();
    }

    private void sendFile(InetAddress dest, String fileName, boolean local) {
        // TODO use REST? to let other node know a file will be sent + name
        // TODO namingserver chooses port

        String path;

        if (local)
            path = localDir;
        else
            path = replicaDir;

        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, 10000);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader and writer to read from and write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            // Make an array of bytes and store the file in said array
            File file = new File(path + fileName);
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            fileInputStream.read(bytes, 0, bytes.length);

            // Let the client know how much bytes will be sent
            writer.println(bytes.length);

            // Send the bytes
            out.write(bytes);
            out.flush();

            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void receiveFile(boolean request) {
        // TODO get filename from rest
        String fileName = "temp";
        String path;

        if (request)
            path = requestDir;
        else
            path = replicaDir;

        try {
            ServerSocket s = new ServerSocket(10000);

            Socket socket = s.accept();

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader and writer to read from and write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

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
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(path + fileName));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();

            socket.close();
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestFile() {

    }

    public void run() {
        discovery();
        bootstrap();

        boolean quit = false;

        while (!quit) {

        }


        shutdown();
    }
}
