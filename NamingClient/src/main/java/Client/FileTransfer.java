package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;

public class FileTransfer {
    private final String localDir;
    private final String replicaDir;
    private final String requestDir;
    private final InetAddress prevNode;
    private final HashSet<String> localFiles;
    private final HashSet<String> replicatedFiles;

    public FileTransfer(String localDir, String replicaDir, String requestDir, InetAddress prevNode) {
        this.localDir = localDir;
        this.replicaDir = replicaDir;
        this.requestDir = requestDir;
        this.prevNode = prevNode;
        localFiles = new HashSet<>();
        File[] files = new File(localDir).listFiles();
        if (files != null) {
            for (File file : files) {
                String fileName = file.getPath().replaceAll(localDir, "");
                localFiles.add(fileName);
            }
        }
        replicatedFiles = new HashSet<>();
    }

    public void sendReplication(InetAddress dest, String fileName) {
        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, 12345);

            // Get the outputstream from the socket
            OutputStream out = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(out, true);

            writer.println("File_replicate");
            writer.println(fileName);

            // Make an array of bytes and store the file in said array
            File file = new File(localDir + fileName);
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
        }
    }

    public void sendOnShutdown(String fileName) {
        try {
            // Create a socket to communicate
            Socket socket = new Socket(prevNode, 20202);

            // Get the outputstream from the socket
            OutputStream out = socket.getOutputStream();

            PrintWriter writer = new PrintWriter(out, true);

            writer.println("File_replicate_on_shutdown: ");
            writer.println(fileName);

            // Make an array of bytes and store the file in said array
            File file = new File(replicaDir + fileName);
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
        }
    }

    public void receiveReplication(Socket socket) {
        try {
            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            String fileName = reader.readLine();

            // Read the size of the requested file the server sent back and create a byte array of that size
            int len = Integer.parseInt(reader.readLine());
            System.out.println("Lengte file: " + len);
            byte[] bytes = new byte[len];

            int bytesRead;
            int current = 0;

            // Read the file from the socket
            do {
                bytesRead = in.read(bytes, current, (bytes.length - current));
                current += bytesRead;
            } while (bytesRead > 0 && current < len);

            // Create an outputstream to write files to the socket
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(replicaDir + fileName));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();
            /*if (localFiles.contains(fileName) || (fileName.startsWith("log_") && localFiles.contains(fileName.substring(4)))) {
                sendOnShutdown(fileName);
                File file = new File(replicaDir + fileName);
                file.delete();
            } else {
                replicatedFiles.add(fileName);
            }*/

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendRequestedFile(Socket socket) {
        String path;

        try {
            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a writer to write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            path = replicaDir;

            String fileName = reader.readLine();

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

    public void requestFile(InetAddress dest, String fileName) {
        String path;

        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, 12345);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            path = requestDir;
            writer.println("File_request");

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
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(path + fileName));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
