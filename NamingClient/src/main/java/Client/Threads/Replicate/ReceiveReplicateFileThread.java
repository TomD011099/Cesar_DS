package Client.Threads.Replicate;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;

public class ReceiveReplicateFileThread extends Thread {
    private Socket socket;
    private String dir;
    private HashSet<String> localFileSet;
    private InetAddress prevNode;

    public ReceiveReplicateFileThread(Socket socket, String dir, HashSet<String> localFileSet, InetAddress prevNode) {
        this.socket = socket;
        this.dir = dir;
        this.localFileSet = localFileSet;
        this.prevNode = prevNode;
    }

    @Override
    public void run() {
        try {
            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String fileName = reader.readLine();
            File file = new File(dir + fileName);
            file.createNewFile();

            OutputStream fileOut = new FileOutputStream(file);

            // Create a buffer
            byte[] buf = new byte[4096];

            int count;
            while ((count = in.read(buf)) > 0) {
                fileOut.write(buf, 0, count);
            }

            /*
            if (localFileSet.contains(fileName) || (fileName.startsWith("log_") && localFileSet.contains(fileName.substring(4, fileName.length() - 8)))) {
                Thread sendReplicateFileThread = new SendReplicateFileThread(prevNode, dir, fileName);
                sendReplicateFileThread.start();
                File file = new File(dir + fileName);
                file.delete();
            }*/

            reader.close();
            fileOut.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            out.append(String.format("0x%02X ", b));
        }

        return out.toString();
    }
}
