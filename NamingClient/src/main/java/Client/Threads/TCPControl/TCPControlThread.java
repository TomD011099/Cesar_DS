package Client.Threads.TCPControl;

import Client.Client;

import java.io.*;
import java.net.*;

/**
 * The main hub for all small TCP communications between nodes
 */
public class TCPControlThread extends Thread {
    private Socket socket;      //A socket to handle the transmissions
    private Client client;      //The instance of client to invoke methods

    /**
     * The only constructor for TCPControl
     *
     * @param socket the socket
     * @param client The instance of client
     */
    public TCPControlThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    /**
     * The main loop for the thread,
     * this loop will read the first message of each communication and based on that invoke a method.
     */
    public void run() {
        try {
            // Create a reader and writer to read from and to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream());

            //Read the command
            String command = reader.readLine();

            switch (command) {
                case "Update":
                    //TODO update set...Node to set... --> this also updates the ID
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
                    File file = new File(client.getReplicaDir() + filename);
                    File logFile = new File(client.getReplicaDir() + logFilename);
                    deleteFile(file);
                    deleteFile(logFile);
                    break;
                case "Update_file":
                    //A file has been edited locally, delete that file to prepare for transmission of the new file
                    String updateFilename = reader.readLine();
                    File updateFile = new File(client.getReplicaDir() + updateFilename);
                    deleteFile(updateFile);
                    break;
                default:
                    //Unknown command
                    System.err.println("Received unknown command: " + command);
            }

            //Close all connections
            reader.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e);
            e.printStackTrace();
        }

        System.out.println("TCPControl Thread ended");
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
}
