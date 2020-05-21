package Client;

import Client.Threads.*;
import Client.Threads.Multicast.*;
import Client.Threads.Replicate.*;
import Client.Threads.Request.*;
import Client.Threads.TCPControl.*;
import Client.Util.*;
import Client.Util.Ports;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * <h1>The main client class</h1>
 * <p>Here, the bulk of the logic is handled, threads are created and files are sent and received</p>
 */
public class Client {
    private RestClient restClient;              //A RESTClient for REST communication
    private final String name;                  //The name of the client
    private InetAddress currentIP;              //The ip address of the node
    private InetAddress prevNode;               //The ip address of the previous node
    private InetAddress nextNode;               //The ip address of the next node
    private InetAddress serverIp;               //The ip address of the server

    private ReplicateServer replicateServer;    //The replicate serversocket
    private RequestServer requestServer;        //The request serversocket
    private TCPControlServer tcpControl;        //TCPControl

    private final int currentID;                //The ID of the node
    private int prevID;                         //The ID of the previous node
    private int nextID;                         //The ID of the next node
    private final String replicaDir;            //The directory where the replicated files are stored   [absolute path]
    private final String localDir;              //The directory from where we'll replicate the files    [absolute path]
    private final String requestDir;            //The 'Download' directory                              [absolute path]
    private final HashSet<String> localFileSet; //A set of all local files
    private MulticastReceiver multicastReceiver;//Thread for receiving multicast messages

    /**
     * The constructor for client
     *
     * @param localDir   The directory from where we'll replicate the files [absolute path]
     * @param replicaDir The directory where the replicated files are stored [absolute path]
     * @param requestDir The 'Download' directory [absolute path]
     * @param name       The name of the client
     * @param ip         The ip adres of the client
     */
    public Client(String localDir, String replicaDir, String requestDir, String name, String ip) {
        try {
            this.currentIP = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        this.name = name;
        this.currentID = new CesarString(this.name).hashCode();
        this.replicaDir = replicaDir;
        this.localDir = localDir;
        this.requestDir = requestDir;

        //Initialize localFileSet and add the files
        localFileSet = new HashSet<>();
        File[] files = new File(localDir).listFiles();
        if (files != null) {
            for (File file : files) {
                String tempName = file.getName();
                localFileSet.add(tempName);
            }
        }

        //Initialize serversockets
        try {
            replicateServer = new ReplicateServer(this);
            Thread replicateServerThread = new Thread(replicateServer, "replicateServer");
            replicateServerThread.start();

            requestServer = new RequestServer(this);
            Thread requestServerThread = new Thread(requestServer, "requestServer");
            requestServerThread.start();

            tcpControl = new TCPControlServer(this);
            Thread tcpControlServerThread = new Thread(tcpControl, "TCPControl");
            tcpControlServerThread.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        UpdateThread updateThread = new UpdateThread(this, localDir);
        updateThread.start();
    }

    /**
     * Get the replica directory
     *
     * @return The replica directory [absolute path]
     */
    public String getReplicaDir() {
        return replicaDir;
    }

    /**
     * Get the local directory
     *
     * @return The local directory [absolute path]
     */
    public String getLocalDir() {
        return localDir;
    }

    /**
     * Get the set of all local files
     *
     * @return The set of all local files [relative to localDir]
     */
    public HashSet<String> getLocalFileSet() {
        return localFileSet;
    }

    /**
     * Get the requested files directory
     *
     * @return The requested file directory [absolute]
     */
    public String getRequestDir() {
        return requestDir;
    }
    
    /**
     * Get the ip address of the previous node
     *
     * @return the ip address of the previous node
     */
    public InetAddress getPrevNode() {
        return prevNode;
    }

    /**
     * Get the ip address of the next node
     *
     * @return the ip address of the next node
     */
    public InetAddress getNextNode() {
        return nextNode;
    }

    /**
     * Check if the client is connected to the server
     *
     * @return a boolean determining if the client is connected to a server
     */
    public boolean isConnected(){
        return serverIp != null;
    }

    /**
     * Set the prevNode
     *
     * @param prevNode The InetAddress of the new prevNode
     */
    public void setPrevNode(InetAddress prevNode) {
        //TODO only use setPrev
        this.prevNode = prevNode;
    }

    /**
     * Set the nextNode
     *
     * @param nextNode The InetAddress of the new nextNode
     */
    public void setNextNode(InetAddress nextNode) {
        //TODO only use setNext
        this.nextNode = nextNode;
    }

    /**
     * Set nextNode and nextID
     *
     * @param nodeName The new nodeName, from this the ID will be calculated
     * @param nextNode The InetAddress of the new nextNode
     */
    public void setNext(String nodeName, InetAddress nextNode) {
        nextID = new CesarString(nodeName).hashCode();
        this.nextNode = nextNode;
        System.out.println("setNext" +
                "\n  prevNode: " + this.prevNode +
                "\n  nextNode: " + this.nextNode);
    }

    /**
     * Set nextNode and nextID
     *
     * @param nodeName The new nodeName, from this the ID will be calculated
     * @param prevNode The InetAddress of the new nextNode
     */
    public void setPrev(String nodeName, InetAddress prevNode) {
        prevID = new CesarString(nodeName).hashCode();
        this.prevNode = prevNode;
        System.out.println("setPrev" +
                "\n  prevNode: " + this.prevNode +
                "\n  nextNode: " + this.nextNode);
    }

    /**
     * Send name to all nodes using multicast ip-address can be extracted from message
     */
    private void discovery() {
        MulticastPublisher publisher = new MulticastPublisher();
        try {
            publisher.multicast(name);

            // Receiving the number of nodes from the server
            DiscoveryThread discoveryThread = new DiscoveryThread(this);

            // Receiving previous and next node from other nodes (if they exist)
            BootstrapThread bootstrapThreadNext = new BootstrapThread(true, this);
            BootstrapThread bootstrapThreadPrev = new BootstrapThread(false, this);

            discoveryThread.start();

            bootstrapThreadNext.start();
            bootstrapThreadPrev.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * End the node correctly
     */
    public void shutdown() {
        //Replication part of shutdown

        //Get all files in replicaDir
        File[] files = new File(replicaDir).listFiles();

        //Send each file to the previous node
        if (files != null) {
            for (File file : files) {
                String fileName = file.getName();
                Thread sendReplicateThread = new SendReplicateFileThread(prevNode, replicaDir, fileName);
                sendReplicateThread.start();
            }
        }

        //Get all localfiles
        File[] localFiles = new File(localDir).listFiles();

        //Let each node that has one of your local files
        if (localFiles != null) {
            for (File file : localFiles) {
                if (!file.getName().startsWith("log_")) {
                    String fileName = file.getName();
                    try {
                        InetAddress replicaIP = InetAddress.getByName(restClient.get("file?filename=" + fileName));
                        // If the server says that the file is replicated on yourself, the real place is in the prevNode
                        if (replicaIP == currentIP)
                            replicaIP = prevNode;
                        sendString(replicaIP, Ports.tcpControlPort, "localShutdown", fileName);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        //Discovery part of shutdown, let your neighbors know who their new neighbors will be
        if (currentID != nextID && currentID != prevID) {
            updateNeighbor(true);
            updateNeighbor(false);
        }
        restClient.delete("unregister?name=" + name);
    }

    /**
     * Method is invoked in DiscoveryThread when an answer to the multicast is received
     *
     * @param numberOfNodes Amount of nodes in the network
     * @param ipServer      The ip address of the server
     */
    public void discoveryResponse(int numberOfNodes, InetAddress ipServer) {
        // Make a new restClient since the server ip is known, also set the server IP
        serverIp = ipServer;
        restClient = new RestClient(serverIp.toString().substring(1));
        if (numberOfNodes < 1) {
            // We are the only node in the network
            prevNode = currentIP;
            nextNode = currentIP;
            prevID = currentID;
            nextID = currentID;
            System.out.println("We are the only node!" +
                    "\n  My nextNode: " + nextNode +
                    "\n  My prevNode: " + prevNode);
        } else {
            System.out.println("I've got more friends (naming server, you're my best friend)");
        }

        initReplicateFiles();
    }

    /**
     * <p>Used to send one command and exactly one string to TCPControl.</p>
     * <b>Available commands and the expected strings:</b>
     * <ul>
     *   <li>Update
     *      <ul>
     *          <li><u>Info:</u> Let your neighbour know who their previous/next node will be</li>
     *          <li><u>String expects:</u> "[prev/next] Inetaddress.toString()"</li>
     *      </ul>
     *   </li>
     *   <li>localShutdown
     *      <ul>
     *          <li><u>Info:</u> This will invoke Client.ownerShutdown() on the destination</li>
     *          <li><u>String expects:</u> "fileName"</li>
     *      </ul>
     *   </li>
     *   <li>Delete_file
     *      <ul>
     *          <li><u>Info:</u> A file you have locally has been deleted, let the node that hosts your file know so it can delete it too</li>
     *          <li><u>String expects:</u> "fileName"</li>
     *      </ul>
     *   </li>
     *   <li>Update_file
     *      <ul>
     *          <li><u>Info:</u> A file you have locally has been updated, let the other node know it has to delete it because the new one will be sent momentarily</li>
     *          <li><u>String expects:</u> "fileName"</li>
     *      </ul>
     *   </li>
     * </ul>
     *
     * @param ip      The destination ip address
     * @param port    The destination port
     * @param command The command to be sent
     * @param string  The data to be sent
     */
    private void sendString(InetAddress ip, int port, String command, String string) {
        try {
            //Create a socket
            Socket socket = new Socket(ip, port);

            // Create a writer to write to the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Send the command, if there is one
            if (!command.equals("")) {
                writer.println(command);
            }

            //Send the string
            writer.println(string);

            writer.close();
            socket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Same as the other sendString, just for destinations other than TCPControl
     *
     * @param ip     The destination ip address
     * @param port   The destination port
     * @param string The data to be sent
     */
    private void sendString(InetAddress ip, int port, String string) {
        sendString(ip, port, "", string);
    }

    private void sendFilesForNextNode() {
        File[] files = new File(replicaDir).listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.getName().startsWith("log_")) {
                    CesarString filenameCesar = new CesarString(file.getName());
                    String filename = file.getName();
                    if (filenameCesar.hashCode() > nextID) {
                        Thread send = new SendReplicateFileThread(nextNode, localDir, filename);
                        Thread sendLog = new SendReplicateFileThread(nextNode, localDir, "log_" + filename + ".txt");
                        send.start();
                        sendLog.start();

                        File logFile = new File(getReplicaDir() + "log_" + filename + ".txt");
                        logFile.delete();
                        file.delete();
                    }
                }
            }
        }
    }

    /**
     * Invoked when MulticastReceiver gets a message, this method is used for setting prev- and nextNode when a new node joins
     *
     * @param nodeName The name of the joining node
     * @param ip       The ip address of the joining node
     */
    public void handleMulticastMessage(String nodeName, InetAddress ip) {
        //Calculate the ID of the new node
        int hash = new CesarString(nodeName).hashCode();
        System.out.println("\nMulticast received!" +
                "\n  nodeName  " + nodeName +
                "\n  ip        " + ip +
                "\n  currentID " + currentID +
                "\n  nextID    " + nextID +
                "\n  prevID    " + prevID +
                "\n  hash      " + hash);

        //See where in the ring the new node is located. If it's a neighbor, change your prev- and/or nextNode
        if (((currentID < hash) && (hash < nextID)) || ((nextID < currentID) && ((hash < nextID) || (hash > currentID)))) {
            //If the new node is our nextNode
            nextNode = ip;
            nextID = hash;
            // Send we are previous node
            sendString(ip, Ports.bootstrapPrevPort, name);
            // Send the right remote files to the next node
            sendFilesForNextNode();
            System.out.println("We are the previous node" +
                    "\n  My nextNode: " + nextNode +
                    "\n  My prevNode: " + prevNode);
        } else if (((prevID < hash) && (hash < currentID)) || ((currentID < prevID) && ((prevID < hash) || (hash < currentID)))) {
            //If the new node is our prevNode
            prevNode = ip;
            prevID = hash;
            // Let the joining node know we are its next node
            sendString(ip, Ports.bootstrapNextPort, name);
            System.out.println("We are the next node" +
                    "\n  My nextNode: " + nextNode +
                    "\n  My prevNode: " + prevNode);
        } else if (nextID == currentID && prevID == currentID) {
            // If there is only one node in the network already
            prevNode = ip;
            nextNode = ip;
            prevID = hash;
            nextID = hash;
            // Let the joining node know we are both its previous and next node
            sendString(ip, Ports.bootstrapPrevPort, name);
            sendString(ip, Ports.bootstrapNextPort, name);
            System.out.println("One friend" +
                    "\n  My nextNode: " + nextNode +
                    "\n  My prevNode: " + prevNode);
        } else {
            // The new node is not my neighbor
            System.out.println("We are not a neighbor" +
                    "\n  My nextNode: " + nextNode +
                    "\n  My prevNode: " + prevNode);
        }
    }

    /**
     * Ask the namingserver where a file is/should be located
     *
     * @param filename The file from which we want to know its location
     * @return The ip address of the node where the file is/should be hosted
     */
    public String requestFileLocation(String filename) {
        System.out.println("Filename request: " + filename);
        return restClient.get("file?filename=" + filename);
    }

    /**
     * Start the replication of files, invoked when this node is created
     */
    private void initReplicateFiles() {
        File[] files = new File(localDir).listFiles();
        if (files != null) {
            for (File file : files) {
                try {
                    if (!file.getName().startsWith("log_")) {
                        String fileName = file.getName();
                        String logFilename = makeLogFile(fileName);
                        InetAddress location = InetAddress.getByName(requestFileLocation(fileName).replaceAll("/", ""));
                        System.out.println("Replication destination of " + fileName + ": " + location);
                        Thread send = new SendReplicateFileThread(location, localDir, fileName);
                        Thread sendLog = new SendReplicateFileThread(location, localDir, logFilename);
                        send.start();
                        sendLog.start();
                    }
                } catch (UnknownHostException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    /**
     * Let your neighbors know who their neighbors will become, because you're leaving
     *
     * @param isDestNextNode Is the message intended for this node's next node?
     */
    private void updateNeighbor(boolean isDestNextNode) {
        try {
            String out;
            InetAddress destIp;
            if (isDestNextNode) {
                out = "prev " + prevNode;
                destIp = nextNode;
            } else {
                out = "next " + nextNode;
                destIp = prevNode;
            }

            sendString(destIp, Ports.tcpControlPort, "Update", out);

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Invoked when UpdateThread detects a local file is created.
     * This will replicate the file.
     *
     * @param filename The filename of the file that has been created
     */
    public void localFileCreated(String filename) {
        // Check if the file itself is not a log file to avoid recursion
        // Also check if the file is not a swap file (Linux only), because they're not meant to be sent
        if (!filename.startsWith("log_") && !filename.contains(".swp")) {
            System.out.println("File created: " + filename);

            try {
                System.out.println("Local file created: " + filename);

                localFileSet.add(filename);
                // Request the location where the file should be replicated
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));
                System.out.println("Send file to: " + location);

                // Make the logfile
                String logFilename = makeLogFile(filename);

                // Send the logfile and other file to the destination
                Thread send = new SendReplicateFileThread(location, localDir, filename);
                Thread sendLog = new SendReplicateFileThread(location, localDir, logFilename);
                send.start();
                sendLog.start();
            } catch (UnknownHostException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Invoked when UpdateThread detects a local file is deleted.
     * This will let the hosting file know it has to delete the file.
     *
     * @param filename The filename of the file that has been deleted
     */
    public void localFileDeleted(String filename) {
        // Check if the file itself is not a log file because we ignore them
        // Also check if the file is not a swap file (Linux only), because they're not meant to be sent
        if (!filename.startsWith("log_") && !filename.contains(".swp")) {
            System.out.println("Local file delted: " + filename);

            try {
                localFileSet.remove(filename);
                File logFile = new File(getLocalDir() + "log_" + filename + ".txt");
                logFile.delete();

                // Request the location where the file is stored
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));

                // Send unicast message to delete file
                sendString(location, Ports.tcpControlPort, "Delete_file", filename);

            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Invoked when UpdateThread detects a local file is modified.
     * This will replicate the file after telling the node that hosts the file to delete said file (to prevent errors).
     *
     * @param filename The filename of the file that has been modified
     */
    public void localFileModified(String filename) {
        // Send only the updated file and don't change the log-file
        // Also check if the file is not a swap file (Linux only), because they're not meant to be sent
        if (!filename.startsWith("log_") && !filename.contains(".swp")) {
            System.out.println("Local file updated: " + filename);

            try {
                // Request the location where the file should be replicated
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));

                // This will only delete the file on the replication node and not the logfile
                sendString(serverIp, Ports.tcpControlPort, "Update_file", filename);

                // Send the updated file
                Thread send = new SendReplicateFileThread(location, localDir, filename);
                send.start();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a logfile for a file
     *
     * @param filename The file for which a logfile has to be created
     * @return The name of the logfile
     */
    private String makeLogFile(String filename) {
        String logFilename = "log_" + filename + ".txt";
        try {
            List<String> lines = Arrays.asList(currentIP.toString(), "false");
            Path file = Paths.get(localDir + logFilename);
            Files.write(file, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logFilename;
    }

    /**
     * A node from which this node is hosting a file is shutting down.
     *
     * @param fileName The file that will be deleted
     */
    public void ownerShutdown(String fileName) {
        File[] files = new File(replicaDir).listFiles();

        if (files != null) {
            // Iterate over all files
            for (File file : files) {
                String tempName = file.getName();
                //Find log files
                if (tempName.contains("log_" + fileName)) {
                    try {
                        BufferedReader br = new BufferedReader(new FileReader(file));
                        br.readLine();
                        // Have they been downloaded?
                        boolean downloaded = Boolean.parseBoolean(br.readLine());
                        // Check whether the remote file is downloaded or not
                        if (!downloaded) {
                            //If not, delete everything
                            if (!file.delete()) {
                                System.err.println("ERR: File " + file.getAbsolutePath() + " not deleted");
                            }
                            String replicatedFileName = replicaDir + tempName.split("_", 2)[1];
                            int splitPoint = replicatedFileName.lastIndexOf(".");
                            replicatedFileName = replicatedFileName.substring(0, splitPoint);
                            File replicatedFile = new File(replicatedFileName);
                            File logFile = new File(getReplicaDir() + "log_" + replicatedFileName + ".txt");
                            if (!replicatedFile.delete()) {
                                System.err.println("ERR: File " + file.getAbsolutePath() + " not deleted");
                            }
                            if (!logFile.delete()) {
                                System.err.println("ERR: File " + logFile.getAbsolutePath() + " not deleted");
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * Main run functionality for running in CMD
     *
     * @throws UnknownHostException When the given ip address is invalid
     */
    public void run() throws UnknownHostException {
        discovery();

        // Create a multicast receiver for client
        MulticastReceiver multicastReceiver = new MulticastReceiver(this);
        Thread receiverThread = new Thread(multicastReceiver);
        receiverThread.start();
        System.out.println("receiverThread started!");

        boolean quit = false;
        Scanner sc = new Scanner(System.in);
        String input;

        while (!quit) {
            System.out.println("\n\nGive the file path you want to access: (press x to stop)");
            input = sc.nextLine();
            if (!input.isEmpty() && !input.equals("x")) {
                String location = requestFileLocation(input);

                Thread requestFileThread = new RequestFileThread(InetAddress.getByName(location.substring(1)), input, requestDir);
                requestFileThread.start();

                System.out.println("Location: " + location);
            } else if (input.equals("x")) {
                quit = true;
            }
        }
        System.out.println("Shutdown");
        shutdown();
        replicateServer.stop();
        requestServer.stop();
        tcpControl.stop();
        multicastReceiver.stop();
        //TODO client doesn't stop
    }

    public void runGraphic() throws UnknownHostException {
        discovery();
        // Create a multicast receiver for client
        multicastReceiver = new MulticastReceiver(this);
        Thread receiverThread = new Thread(multicastReceiver);
        receiverThread.start();
        System.out.println("receiverThread started!");
    }

    public void stopGraphic() {
        shutdown();
        tcpControl.stop();
        multicastReceiver.stop();
    }
}