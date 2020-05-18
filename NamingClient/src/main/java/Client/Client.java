package Client;

import Client.Threads.*;
import org.springframework.web.bind.annotation.RestController;

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
 * The main client class
 */
public class Client {
    private RestClient restClient;              //A RESTclient for REST communication
    private final String name;                  //The name of the client
    private InetAddress currentIP;              //The ip address of the node
    private InetAddress prevNode;               //The ip address of the previous node
    private InetAddress nextNode;               //The ip address of the next node
    private InetAddress serverIp;               //The ip address of the server
    private TCPControl tcpControl;              //The TCPController
    private final int currentID;                //The ID of the node
    private int prevID;                         //The ID of the previous node
    private int nextID;                         //The ID of the next node
    private final String replicaDir;            //The directory where the replicated files are stored   [absolute path]
    private final String localDir;              //The directory from where we'll replicate the files    [absolute path]
    private final String requestDir;            //The 'Download' directory                              [absolute path]
    private final HashSet<String> localFileSet; //A set of all local files
    private ArrayList<ArrayList<String>> filesInSystem; // All the files in the system Y

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
     * The constructor for client
     *
     * @param localDir The directory from where we'll replicate the files [absolute path]
     * @param replicaDir The directory where the replicated files are stored [absolute path]
     * @param requestDir The 'Download' directory [absolute path]
     * @param name The name of the client
     * @param ip The ip adres of the client
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
        for (File file : Objects.requireNonNull(files)) {
            String tempName = file.getAbsolutePath().replace('\\', '/').replaceAll(replicaDir, "");
            localFileSet.add(tempName);
        }

        //Initialize TCPControl
        try {
            tcpControl = new TCPControl(12345, this);
            Thread t1 = new Thread(tcpControl, "TCPControl");
            t1.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        filesInSystem = new ArrayList<>();

        UpdateThread updateThread = new UpdateThread(this, localDir);
        updateThread.start();
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

            System.out.println("send multicast with name");

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
        for (File file : Objects.requireNonNull(files)) {
            String fileName = file.getAbsolutePath().replace('\\', '/').replaceAll(replicaDir, "");
            Thread sendReplicateThread = new SendReplicateFileThread(prevNode, replicaDir, fileName);
            sendReplicateThread.start();
        }

        //Get all localfiles
        File[] localFiles = new File(localDir).listFiles();

        //Let each node that has one of your local files
        for (File file : Objects.requireNonNull(localFiles)) {
            String fileName = file.getAbsolutePath().replace('\\', '/').replaceAll(localDir, "");
            try {
                InetAddress replicaIP = InetAddress.getByName(restClient.get("file?filename=" + fileName).substring(1));
                sendString(12345, fileName, replicaIP, "LocalShutdown");
            } catch (UnknownHostException e) {
                e.printStackTrace();
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
     * TODO
     */
    public void failure() {

    }

    /**
     * Method is invoked in DiscoveryThread when an answer to the multicast is received
     *
     * @param numberOfNodes Amount of nodes in the network
     * @param ipServer The ip address of the server
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
            System.out.println("We are the only node!");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        } else {
            System.out.println("I've more friends");
        }

        initReplicateFiles();
    }

    private void sendString(int port, String string, InetAddress ip, String command) {
        try {
            Socket socket = new Socket(ip, port);

            // Create a writer to write to the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Send your name
            if (!command.equals("")) {
                writer.println(command);
            }
            writer.println(string);
            System.out.println("Data sent: " + string);

            writer.close();
            socket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendString(int port, String string, InetAddress ip) {
        sendString(port, string, ip, "");
    }

    public void handleMulticastMessage(String nodeName, InetAddress ip) {
        int hash = new CesarString(nodeName).hashCode();
        System.out.println("Multicast received!");
        System.out.println("nodeName received: " + nodeName);
        System.out.println("ip received: " + ip);
        System.out.println("currentID: " + currentID);
        System.out.println("nextID " + nextID);
        System.out.println("prevID " + prevID);
        System.out.println("hash: " + hash);
        if (((currentID < hash) && (hash < nextID)) || ((nextID < currentID) && ((hash < nextID) || (hash > currentID)))) {
            nextNode = ip;
            nextID = hash;
            // Send we are previous node
            sendString(11111, name, ip);
            System.out.println("We are previous node");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        } else if (((prevID < hash) && (hash < currentID)) || ((currentID < prevID) && ((prevID < hash) || (hash < currentID)))) {
            prevNode = ip;
            prevID = hash;
            // Send we are next node
            sendString(56789, name, ip);
            System.out.println("We are next node");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        } else if (nextID == currentID && prevID == currentID) {
            // There are two nodes in the network
            prevNode = ip;
            nextNode = ip;
            prevID = hash;
            nextID = hash;
            sendString(11111, name, ip);
            sendString(56789, name, ip);
            System.out.println("One friend");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        }
    }

    public String requestFileLocation(String filename) {
        System.out.println("Filename request: " + filename);
        return restClient.get("file?filename=" + filename);
    }

    public void setPrevNode(InetAddress prevNode) {
        this.prevNode = prevNode;
    }

    public void setNextNode(InetAddress nextNode) {
        this.nextNode = nextNode;
    }

    public void setNext(String nodeName, InetAddress nextNode) {
        nextID = new CesarString(nodeName).hashCode();
        this.nextNode = nextNode;
        System.out.println("setNext");
        System.out.println("prevNode: " + this.prevNode);
        System.out.println("nextNode: " + this.nextNode);
    }

    public void setPrev(String nodeName, InetAddress prevNode) {
        prevID = new CesarString(nodeName).hashCode();
        this.prevNode = prevNode;
        System.out.println("setPrev");
        System.out.println("prevNode: " + this.prevNode);
        System.out.println("nextNode: " + this.nextNode);
    }

    public InetAddress getPrevNode() {
        return prevNode;
    }

    public InetAddress getNextNode() {
        return nextNode;
    }

    private void initReplicateFiles() {
        File[] files = new File(localDir).listFiles();
        for (File file : Objects.requireNonNull(files)) {
            try {
                String fileName = file.getAbsolutePath();
                fileName = fileName.replace('\\', '/').replaceAll(localDir, "");
                InetAddress location = InetAddress.getByName(requestFileLocation(fileName).replaceAll("/", ""));
                System.out.println("location: " + location);
                Thread send = new SendReplicateFileThread(location, localDir, fileName);
                send.start();
            } catch (UnknownHostException e) {
                System.err.println(e.getMessage());
            }
        }
    }

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

            Socket socket = new Socket(destIp, 12345);

            // Create a writer to write to the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            writer.println("Update");
            writer.println(out);
            System.out.println("Data sent: " + out);

            writer.close();
            socket.close();

        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void localFileCreated(String filename) {

        System.out.println("Filename van ons: " + filename);

        // Check if the file itself is not a log file to avoid recursion
        if (!filename.startsWith("log_")) {
            try {
                localFileSet.add(filename);

                // Request the location where the file should be replicated
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));
                System.out.println("location new created: " + location);

                // Make the log-file
                String logFilename = makeLogFile(filename);

                // Send the log-file and other file to the destination
                Thread send = new SendReplicateFileThread(location, localDir, filename);
                send.start();
                Thread sendLog = new SendReplicateFileThread(location, localDir, logFilename);
                sendLog.start();
            } catch (UnknownHostException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void localFileDeleted(String filename) {
        // Check if the file itself is not a log file to avoid recursion
        if (!filename.startsWith("log_")) {
            try {
                localFileSet.remove(filename);

                // Request the location where the file is stored
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));

                // Send unicast message to delete file
                Socket socket = new Socket(location, 12345);

                OutputStream out = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out, true);
                //writer.println("Delete_file");
                //writer.println(filename);

                socket.close();
                writer.close();
                out.close();

            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public void localFileModified(String filename) {
        // Send only the updated file and don't change the log-file
        try {
            Socket socket = new Socket(serverIp, 12345);

            // Request the location where the file should be replicated
            InetAddress location = InetAddress.getByName(requestFileLocation(filename));

            OutputStream out = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(out, true);

            // This will only delete the file on the replication node and not the log-file
            //writer.println("Update_file");
            //writer.println(filename);

            socket.close();
            writer.close();
            out.close();

            // Send the updated file
            Thread send = new SendReplicateFileThread(location, localDir, filename);
            send.start();

        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

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

    public void ownerShutdown(String fileName) {
        File[] files = new File(replicaDir).listFiles();

        for (File file : Objects.requireNonNull(files)) {
            String tempName = file.getAbsolutePath().replace('\\', '/').replaceAll(replicaDir, "");
            if (tempName.contains("log_" + fileName)) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    br.readLine();
                    boolean downloaded = Boolean.parseBoolean(br.readLine());
                    if (!downloaded) {
                        if (!file.delete()) {
                            System.err.println("ERR: File " + file.getAbsolutePath() + " not deleted");
                        }
                        String replicatedFileName = replicaDir + tempName.split("_", 2)[1];
                        int splitPoint = replicatedFileName.lastIndexOf(".");
                        replicatedFileName = replicatedFileName.substring(0, splitPoint);
                        File replicatedFile = new File(replicatedFileName);
                        if (!replicatedFile.delete()) {
                            System.err.println("ERR: File " + file.getAbsolutePath() + " not deleted");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void addFilesInSystem(ArrayList<String> subList) {
        filesInSystem.add(subList);
    }

    public void removeFilesInSystem(ArrayList<String> subList) {
        filesInSystem.remove(subList);
    }

    public void updateList() {
        System.out.println("rest ip: " + nextNode.toString().substring(1));
        RestClient nextNodeREST = new RestClient(nextNode.toString().substring(1));
        String listString = nextNodeREST.get("fileList");
        System.out.println(listString);
    }

    public String listToString() {
        return name;
    }

    public void run() throws UnknownHostException {
        discovery();

        // Start the synchAgent
        SynchAgent synchAgent = new SynchAgent(this, replicaDir);
        Thread synchAgentThread = new Thread(synchAgent);
        synchAgentThread.start();

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
        tcpControl.stop();
        multicastReceiver.stop();
        //TODO client doesn't stop
    }
}