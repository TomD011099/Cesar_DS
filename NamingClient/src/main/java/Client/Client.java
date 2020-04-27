package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

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

    public Client(String localDir, String replicaDir, String requestDir, String name, String ip) throws NodeNotRegisteredException {
        try {
            this.currentIP = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.getMessage();
        }

        this.name = name;
        this.currentID = new CesarString(this.name).hashCode();
        this.fileTransfer = new FileTransfer(localDir, replicaDir, requestDir);
        try {
            serverThread = new ServerThread(12345, this);
            Thread t1 = new Thread(serverThread, "T1");
            t1.start();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
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
        updateNeighbor(true);
        updateNeighbor(false);
        restClient.delete("unregister?name=" + name);

        // TODO relocate hosted files that were on the node
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
        } else {
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

                //Temp
                fileTransfer.receiveFile(InetAddress.getByName(location.substring(1)), true, "test.txt");
                
                System.out.println("Location: " + location);
            } else if (input.equals("x")) {
                quit = true;
            }

        }
        shutdown();
        Thread.getAllStackTraces().keySet().forEach((t) -> System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive " + t.isAlive()));
        serverThread.stop();
        multicastReceiver.stop();
        Thread.getAllStackTraces().keySet().forEach((t) -> System.out.println(t.getName() + "\nIs Daemon " + t.isDaemon() + "\nIs Alive " + t.isAlive()));
    }
}