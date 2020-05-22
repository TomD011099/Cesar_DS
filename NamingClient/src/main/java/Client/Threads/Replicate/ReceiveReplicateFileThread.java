package Client.Threads.Replicate;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashSet;

/**
 * The thread that gets created when the ReplicateServer receives a request
 */
public class ReceiveReplicateFileThread extends Thread {
    private Socket socket;                  //The socket on which communication will be done
    private String dir;                     //The directory to which the file will be saved
    private HashSet<String> localFileSet;   //The set of local files for the node
    private InetAddress prevNode;           //The ip address of the previous node

    /**
     * The constructor
     *
     * @param socket       The socket created by the ServerSocket
     * @param dir          The absolute path to replicaDir
     * @param localFileSet The localFileset from client
     * @param prevNode     The location of the previousnode
     */
    public ReceiveReplicateFileThread(Socket socket, String dir, HashSet<String> localFileSet, InetAddress prevNode) {
        this.socket = socket;
        this.dir = dir;
        this.localFileSet = localFileSet;
        this.prevNode = prevNode;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            System.out.println(Thread.currentThread().getName() + " - Receiving file...");

            // Get the inputstream from the socket
            InputStream in = socket.getInputStream();

            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            // Get the filename and create the file
            String fileName = reader.readLine();
            System.out.println(Thread.currentThread().getName() + " - " + fileName);
            File file = new File(dir + fileName);
            file.createNewFile();

            // Create a stream to write to the file
            OutputStream fileOut = new FileOutputStream(file);

            // Create a buffer
            byte[] buf = new byte[4096];

            // Receive the data and write it to the file
            int count;
            while ((count = in.read(buf)) > 0) {
                fileOut.write(buf, 0, count);
                //System.out.println("Receive:\n" + hex(buf)); //Used for debugging
            }

            // Check if the received file is stored locally
            if (localFileSet.contains(fileName)) {
                // Rereolicate the file to the previous node (as required)
                Thread sendReplicateFileThread = new SendReplicateFileThread(prevNode, dir, fileName);
                sendReplicateFileThread.start();
                File f = new File(dir + fileName);
                //Delete the file here
                f.delete();
            } else if (fileName.startsWith("log_")) {
                if (localFileSet.contains(fileName.substring(4, fileName.length() - 4))) {
                    Thread sendLogFileThread = new SendReplicateFileThread(prevNode, dir, fileName);
                    sendLogFileThread.start();
                    File f = new File(dir + fileName);
                    f.delete();
                }
            }

            // Close all connections
            reader.close();
            fileOut.close();
            in.close();
            socket.close();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + " Ended. (RecvRepl)");
    }

    /**
     * Used for debugging filetransfers
     *
     * @param bytes The buffer
     * @return A string of hex values, along with their ASCII values
     */
    private String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);

        for (byte b : bytes) {
            if (b != 0x0)
                out.append((char) b).append(String.format(" 0x%02X\n", b));
        }

        return out.toString();
    }
}
