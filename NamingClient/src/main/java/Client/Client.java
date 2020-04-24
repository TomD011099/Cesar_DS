package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private String localDir;
    private String replicaDir;
    private String requestDir;
    private InetAddress ip;
    private InetAddress prevNode;
    private InetAddress nextNode;
    private String name;
    private InetAddress serverIp;
    private RestClient restClient;
    private ServerThread serverThread;
    private int currentID;
    private int prevID;
    private int nextID;

    public Client(String localDir, String replicaDir, String requestDir, String name, String ip) throws NodeNotRegisteredException {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (Exception e) {
            e.getMessage();
        }

        this.name = name;
        this.currentID = new CesarString(this.name).hashCode();
        this.localDir = localDir;
        this.replicaDir = replicaDir;
        this.requestDir = requestDir;
        try {
            serverThread = new ServerThread(12345, this);
            serverThread.start();
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

    public void discoveryResponse(int numberOfNodes, InetAddress ipServer) {
        // Make a new restClient since the server ip is known, also set the server IP
        serverIp = ipServer;
        restClient = new RestClient(serverIp.toString().substring(1));
        if (numberOfNodes < 1) {
            // We are the only node in the network
            prevNode = ip;
            nextNode = ip;
            prevID = currentID;
            nextID = currentID;
            System.out.println("We are the only node!");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        } else {
            System.out.println("I've more friends (naming server, you're my best friend)");
        }
    }

    public void handleMulticastMessage(String nodeName, InetAddress ip) {
        int hash = new CesarString(nodeName).hashCode();
        System.out.println("Multicast received!");
        System.out.println("nodeName received: " + nodeName);
        System.out.println("ip received: " + ip);
        if ((currentID < hash) && (hash < nextID)) {
            nextNode = ip;
            nextID = hash;
            // Send we are previous node
            sendString(11111, name);
            System.out.println("We are previous node");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        } else if ((prevID < hash) && (hash < currentID)) {
            prevNode = ip;
            prevID = hash;
            // Send we are next node
            sendString(56789, name);
            System.out.println("We are next node");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        } else {
            // There are two nodes in the network
            prevNode = ip;
            nextNode = ip;
            prevID = hash;
            nextID = hash;
            sendString(11111, name);
            sendString(56789, name);
            System.out.println("One friend");
            System.out.println("My nextNode: " + nextNode);
            System.out.println("My prevNode: " + prevNode);
        }
    }

    private void sendString(int port, String string) {
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

    public void setNext(String nodeName, InetAddress nextNode) {
        nextID = new CesarString(nodeName).hashCode();
        this.nextNode = nextNode;
    }

    public void setPrev(String nodeName, InetAddress prevNode) {
        prevID = new CesarString(nodeName).hashCode();
        this.prevNode = prevNode;
    }

    private void failure() {


        shutdown();
    }

    private void sendFile(InetAddress dest, String fileName, boolean local) {
        // TODO use REST? to let other node know a file will be sent + name
        // TODO namingserver chooses port

        String path;

        if (local)
            path = localDir;
        else
            path = replicaDir;

        try {
            // Create a socket to communicate
            Socket socket = new Socket(dest, 10000);

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader and writer to read from and write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            // Make an array of bytes and store the file in said array
            File file = new File(path + fileName);
            byte[] bytes = new byte[(int) file.length()];
            BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(file));
            fileInputStream.read(bytes, 0, bytes.length);

            // Let the client know how much bytes will be sent
            writer.println(bytes.length);

            // Send the bytes
            out.write(bytes);
            out.flush();

            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private void receiveFile(boolean request) {
        // TODO get filename from rest
        String fileName = "temp";
        String path;

        if (request)
            path = requestDir;
        else
            path = replicaDir;

        try {
            ServerSocket s = new ServerSocket(10000);

            Socket socket = s.accept();

            // Get the in- and outputstreams from the socket
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            // Create a reader and writer to read from and write to the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            PrintWriter writer = new PrintWriter(out, true);

            // Read the size of the requested file the server sent back and create a byte array of that size
            int len = Integer.parseInt(reader.readLine());
            byte[] bytes = new byte[len];

            int bytesRead;
            int current = 0;

            // Read the file from the socket
            do {
                bytesRead = in.read(bytes, current, (bytes.length - current));
                current += bytesRead;
            } while (bytesRead > 0 && current < len);

            // Create an outputstream to write files to the socket
            BufferedOutputStream fileOutputStream = new BufferedOutputStream(new FileOutputStream(path + fileName));

            // Create the local file with the data of the downloaded file
            fileOutputStream.write(bytes, 0, bytes.length);
            fileOutputStream.flush();

            socket.close();
            s.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String requestFile(String filename) {
        return restClient.get("file?filename=" + filename);
    }

    public void setPrevNode(InetAddress prevNode) {
        this.prevNode = prevNode;
    }

    public void setNextNode(InetAddress nextNode) {
        this.nextNode = nextNode;
    }

    public InetAddress getPrevNode() {
        return prevNode;
    }

    public InetAddress getNextNode() {
        return nextNode;
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
        String prevRead = "null";
        Scanner sc = new Scanner(System.in);
        String input;

        while (!quit) {
//            String str = serverThread.getRead();
//            System.out.println(str + "\t" + prevRead);
//            if (!str.equals(prevRead) && !str.equals("null")) {
//                String[] parsed = str.split(" ");
//                if (parsed[0].equals("prev")) {
//                    prevNode = InetAddress.getByName(parsed[1].substring(1));
//                    System.out.println("prevNode updated to: " + prevNode);
//                } else if (parsed[0].equals("next")) {
//                    nextNode = InetAddress.getByName(parsed[1].substring(1));
//                    System.out.println("nextNode updated to: " + nextNode);
//                }
//                prevRead = str;
//            }


            System.out.println("\n\nGive the file path you want to access: (press x to stop)");
            input = sc.nextLine();
            if (!input.isEmpty() && !input.equals("x")) {
                String location = requestFile(input);
                System.out.println("Location: " + location);
            } else if (input.equals("x")) {
                quit = true;
            }

        }
        shutdown();
    }
}