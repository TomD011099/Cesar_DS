package Client;

import java.io.*;
import java.net.*;
import Client.Threads.*;

public class TCPControl implements Runnable {
    private final ServerSocket serverSocket;
    private final Client client;
    private volatile boolean stop;

    public TCPControl(int port, Client client) throws IOException {
        serverSocket = new ServerSocket(port);
        this.client = client;
        this.stop = false;
    }

    public void run() {
        try {
            while (!stop) {
                Socket socket = serverSocket.accept();

                // Create a reader to read from the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                String command = reader.readLine();

                switch (command) {
                    case "Update":
                        Thread updateNeighborsThread = new UpdateNeighborsThread(socket, client);
                        updateNeighborsThread.start();
                        break;
                    case "File_replicate":
                        Thread receiveReplicationFileTread = new ReceiveReplicateFileThread(socket, client.getReplicaDir(), client.getLocalFileSet(), client.getPrevNode());
                        receiveReplicationFileTread.start();
                        break;
                    case "localShutdown":
                        String fileName = reader.readLine();
                        socket.close();
                        client.ownerShutdown(fileName);
                        break;
                    case "File_request":
                        Thread sendRequestedFileThread = new SendRequestedFileThread(socket, client.getLocalDir());
                        sendRequestedFileThread.start();
                        break;
                    case "Delete_file":
                        String filename = reader.readLine();
                        String logFilename = "log_" + filename + ".txt";
                        File file = new File("./remote/" + filename);
                        File logFile = new File("./remote/" + logFilename);
                        deleteFile(file);
                        deleteFile(logFile);
                        break;
                    case "Update_file":
                        String updateFilename = reader.readLine();
                        File updateFile = new File("./remote" + updateFilename);
                        deleteFile(updateFile);
                        break;
                    default:
                        System.err.println("Received unknown command: " + command);
                        socket.close();
                }

                reader.close();
            }
        } catch (IOException ioException) {
            client.failure();
        }
    }

    private void deleteFile(File file) {
        if (file.delete())
            System.out.println(file.getName() + " deleted successfully!");
        else
            System.out.println("Could not delete file: " + file.getName());
    }

    public void stop() {
        stop = true;
    }
}
