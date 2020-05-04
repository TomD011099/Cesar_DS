package Client;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import java.io.*;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;

public class Client {
    private final FileTransfer fileTransfer;
    private RestClient restClient;
    private final String name;
    private InetAddress currentIP;
    private InetAddress prevNode;
    private InetAddress nextNode;
    private InetAddress serverIp;
    private ServerThread serverThread;
    private int currentID;
    private int prevID;
    private int nextID;
    private MulticastReceiver multicastReceiver;
    private String replicaDir;
    private String localDir;

    public Client(String localDir, String replicaDir, String requestDir, String name, String ip) throws NodeNotRegisteredException {
        try {
            this.currentIP = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.getMessage();
        }

        this.name = name;
        this.currentID = new CesarString(this.name).hashCode();
        this.fileTransfer = new FileTransfer(localDir, replicaDir, requestDir, prevNode);
        this.replicaDir = replicaDir;
        this.localDir = localDir;
        try {
            serverThread = new ServerThread(12345, this);
            Thread t1 = new Thread(serverThread, "T1");
            t1.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        // TODO put this in the right place
        //UpdateThread updateThread = new UpdateThread(this, localDir);
        //updateThread.start();

        // Create a multicast receiver for client
        multicastReceiver = new MulticastReceiver(this);
        Thread receiverThread = new Thread(multicastReceiver);
        receiverThread.start();
        System.out.println("receiverThread started!");

        //initReplicateFiles();
    }

    private void register(String name, String ip) throws NodeNotRegisteredException {
        switch (Integer.parseInt(restClient.post("register?name=" + name + "&ip=" + ip, null))) {
            case 1:
                System.out.println("Node registered");
                break;
            case -1:
                throw new NodeNotRegisteredException("Node with same hash already exists!");
            case -2:
                throw new NodeNotRegisteredException("Ip address of node not found!");
            default:
                break;
        }
    }

    /* Send name to all nodes using multicast
     *  ip-address can be extracted from message */
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

    private void bootstrap() {

    }

    public void shutdown() {
        //Replication part of shutdown
        /*File dir = new File(replicaDir);
        fetchFiles(dir, file -> {
            String fileName = file.getAbsolutePath().replace('\\', '/').replaceAll(replicaDir, "");
            fileTransfer.sendOnShutdown(fileName);
        });
        dir = new File(localDir);
        fetchFiles(dir, file -> {
            String fileName = file.getAbsolutePath().replace('\\', '/').replaceAll(localDir, "");
            try {
                InetAddress replicaIP = InetAddress.getByName(restClient.get("file?filename=" + fileName).substring(1));
                sendString(12345, "Shutdown:" + fileName, replicaIP);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        });*/

        //Discovery part of shutdown
        if ((currentID != prevID) && (currentID != nextID)) {
            updateNeighbor(true);
            updateNeighbor(false);
        }
        restClient.delete("unregister?name=" + name);

        // TODO relocate hosted files that were on the node
    }

    private void fetchFiles(File dir, Consumer<File> fileConsumer) {
        if (dir.isDirectory()) {
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                fetchFiles(file, fileConsumer);
            }
        } else {
            fileConsumer.accept(dir);
        }
    }


    public void failure() {

    }

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
            System.out.println("I've more friends (naming server, you're my best friend)");
        }
    }

    private void sendString(int port, String string, InetAddress ip) {
        try {
            Socket socket = new Socket(ip, port);

            // Create a writer to write to the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Send your name
            writer.println(string);
            System.out.println("Data sent: " + string);

            writer.close();
            socket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
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
        } else if (nextID == currentID && prevID == currentID){
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

    public FileTransfer getFileTransfer() {
        return fileTransfer;
    }

    private void initReplicateFiles() {
        File dir = new File(localDir);
        fetchFiles(dir, file -> {
            try {
                String fileName = file.getAbsolutePath().replace('\\', '/').replaceAll(localDir, "");
                System.out.println(fileName);
                fileTransfer.sendReplication(InetAddress.getByName(requestFileLocation(fileName).substring(1)), fileName);
            } catch (UnknownHostException e) {
                System.err.println(e.getMessage());
            }
        });
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
        // Check if the file itself is not a log file to avoid recursion
        if (!filename.startsWith("log_")) {
            try {
                // Request the location where the file should be replicated
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));

                // Make the log-file
                String logFilename = makeLogFile(filename);

                // Send the log-file and other file to the destination
                fileTransfer.sendReplication(location, filename);
                fileTransfer.sendReplication(location, logFilename);

            } catch (Exception e) {
                e.getMessage();
            }
        }
    }

    public void localFileDeleted(String filename) {
        // Check if the file itself is not a log file to avoid recursion
        if (!filename.startsWith("log_")) {
            try {
                // Request the location where the file is stored
                InetAddress location = InetAddress.getByName(requestFileLocation(filename));

                // Send unicast message to delete file
                Socket socket = new Socket(location, 12345);

                OutputStream out = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(out, true);
                writer.println("Delete_file");
                writer.println(filename);

                socket.close();
                writer.close();

            } catch (Exception e) {
                e.getMessage();
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
            writer.println("Update_file");
            writer.println(filename);

            socket.close();
            writer.close();

            // Send the updated file
            fileTransfer.sendReplication(location, filename);

        } catch (Exception e) {
            e.getMessage();
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
        File dir = new File(replicaDir);
        fetchFiles(dir, file -> {
            String tempName = file.getAbsolutePath().replace('\\', '/').replaceAll(replicaDir, "");
            if (tempName.contains("log_" + fileName)) {
                try {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    br.readLine();
                    boolean downloaded = Boolean.parseBoolean(br.readLine());
                    if (!downloaded) {
                        file.delete();
                        int splitPoint = tempName.lastIndexOf(".");
                        File replicatedFile = new File(replicaDir + tempName.split("_", 2)[1].substring(0, splitPoint));
                        replicatedFile.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                fileTransfer.sendOnShutdown(fileName);
            }
        });
    }

    public void run() throws UnknownHostException {
        discovery();

        boolean quit = false;
        Scanner sc = new Scanner(System.in);
        String input;

        while (!quit) {
            System.out.println("\n\nGive the file path you want to access: (press x to stop)");
            input = sc.nextLine();
            if (!input.isEmpty() && !input.equals("x")) {
                String location = requestFileLocation(input);
                fileTransfer.requestFile(InetAddress.getByName(location.substring(1)), input);

                System.out.println("Location: " + location);
            } else if (input.equals("x")) {
                quit = true;
            }
        }
        shutdown();
        serverThread.stop();
        multicastReceiver.stop();
        //TODO client doesn't stop
    }
}