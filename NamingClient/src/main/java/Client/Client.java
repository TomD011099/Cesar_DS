package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client {
    private final FileTransfer fileTransfer;
    private final RestClient restClient;
    private final String name;
    private InetAddress ip;
    private InetAddress prevNode;
    private InetAddress nextNode;
    private InetAddress serverIp;
    private ServerThread serverThread;

    public Client(String localDir, String replicaDir, String requestDir, String name, String ip, String server, String nextNode, String prevNode) throws NodeNotRegisteredException {
        try {
            this.serverIp = InetAddress.getByName(server);
            this.ip = InetAddress.getByName(ip);
            //temp
            this.nextNode = InetAddress.getByName(nextNode);
            this.prevNode = InetAddress.getByName(prevNode);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        this.name = name;

        this.fileTransfer = new FileTransfer(localDir, replicaDir, requestDir);

        restClient = new RestClient(serverIp.toString().substring(1));
        register(this.name, this.ip.toString().substring(1));

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

    private void discovery() {

    }

    private void bootstrap() {
        // TODO ask server where to put files
    }

    public void shutdown() {
        updateNeighbor(true);
        updateNeighbor(false);
        restClient.delete("unregister?name=" + name);

        // TODO relocate hosted files that were on the node
    }

    public void failure() {

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
            failure();
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public void run() throws UnknownHostException {
        discovery();
        bootstrap();

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
        serverThread.stop();
    }
}