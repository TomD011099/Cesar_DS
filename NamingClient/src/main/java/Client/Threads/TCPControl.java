package Client.Threads;

import Client.Client;
import Client.Util.Ports;

import java.io.*;
import java.net.*;

/**
 * The main hub for all small TCP communication between nodes
 */
public class TCPControl implements Runnable {
    private ServerSocket serverSocket;    //A serversocket to receive the transmissions
    private Client client;                //The instance of client to invoke methods
    private volatile boolean stop;        //A boolean to end the infinite loop

    /**
     * The only constructor for TCPControl
     *
     * @param client The instance of client
     * @throws IOException When the port isn't a valid input/is busy
     */
    public TCPControl(Client client) throws IOException {
        serverSocket = new ServerSocket(Ports.tcpControlPort);
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
                System.out.println("\nSocket at start " + socket.toString() + "\nChannel: " + socket.getChannel() + "\nIs closed: " + socket.isClosed());

                // Create a reader and writer to read from and to the socket
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter writer = new PrintWriter(socket.getOutputStream());

                //Read the command
                String command = reader.readLine();

                switch (command) {
                    case "Update":
                        //One of your neighbors have stopped and want to let you know who your new neighbor will be
                        String in = reader.readLine();
                        String[] parsed = in.split(" ");
                        if (parsed[0].equals("prev")) {
                            client.setPrevNode(InetAddress.getByName(parsed[1].substring(1)));
                            System.out.println("prevNode updated to: " + client.getPrevNode());
                        } else if (parsed[0].equals("next")) {
                            client.setNextNode(InetAddress.getByName(parsed[1].substring(1)));
                            System.out.println("nextNode updated to: " + client.getNextNode());
                        }

                        break;
                    case "localShutdown":
                        //A node from which you have duplicates has shut down and wants to let you know
                        String fileName = reader.readLine();
                        client.ownerShutdown(fileName);
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
                }

                reader.close();
                writer.close();
                socket.close();
            }

            serverSocket.close();
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
