package Client;

import java.io.*;
import java.net.*;

import Client.Threads.*;

/**
 * The main hub for all TCP communication between nodes
 */
public class TCPControl implements Runnable {
    private final ServerSocket serverSocket;    //A serversocket to receive the transmissions
    private final Client client;                //The instance of client to invoke methods
    private volatile boolean stop;              //A boolean to end the infinite loop

    /**
     * The only constructor for TCPControl
     *
     * @param port   The port on which the socker will listen
     * @param client The instance of client
     * @throws IOException When the port isn't a valid input/is busy
     */
    public TCPControl(int port, Client client) throws IOException {
        serverSocket = new ServerSocket(port);
        this.client = client;
        this.stop = false;
    }

    /**
     * The main loop for the thread,
     * this loop will read the first message of each communication and based on that invoke a method.
     */
    public void run() {
        try {
            while (!stop) {
                //Accept incoming transmissions
                Socket socket = serverSocket.accept();

                // Create a reader to read from the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                //Read the command
                String command = reader.readLine();

                switch (command) {
                    case "Update":
                        //One of your neighbors have stopped and want to let you know who your new neighbor will be
                        Thread updateNeighborsThread = new UpdateNeighborsThread(socket, client);
                        updateNeighborsThread.start();
                        break;
                    case "File_replicate":
                        //A node is trying to replicate a file to this node
                        Thread receiveReplicationFileTread = new ReceiveReplicateFileThread(socket, client.getReplicaDir(), client.getLocalFileSet(), client.getPrevNode());
                        receiveReplicationFileTread.start();
                        break;
                    case "localShutdown":
                        //A node from which you have duplicates has shut down and wants to let you know
                        String fileName = reader.readLine();
                        socket.close();
                        client.ownerShutdown(fileName);
                        break;
                    case "File_request":
                        //Someone wants a file you host
                        Thread sendRequestedFileThread = new SendRequestedFileThread(socket, client.getLocalDir());
                        sendRequestedFileThread.start();
                        break;
                    case "Delete_file":
                        //A file has been deleted locally and has to be deleted on the remote
                        String filename = reader.readLine();
                        String logFilename = "log_" + filename + ".txt";
                        File file = new File("./remote/" + filename);
                        File logFile = new File("./remote/" + logFilename);
                        deleteFile(file);
                        deleteFile(logFile);
                        break;
                    case "Update_file":
                        //A file has been edited locally, delete that file to prepare for transmission of the new file
                        String updateFilename = reader.readLine();
                        File updateFile = new File("./remote" + updateFilename);
                        deleteFile(updateFile);
                        break;
                    default:
                        //Unknown command
                        System.err.println("Received unknown command: " + command);
                        socket.close();
                }

                reader.close();
            }
        } catch (IOException ioException) {
            client.failure();
        }
    }

    /**
     * Delete a specific file
     *
     * @param file The file to be deleted
     */
    private void deleteFile(File file) {
        if (file.delete())
            System.out.println(file.getName() + " deleted successfully!");
        else
            System.out.println("Could not delete file: " + file.getName());
    }

    /**
     * End the loop and the socket
     */
    public void stop() {
        stop = true;
    }
}
