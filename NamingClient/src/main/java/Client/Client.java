package Client;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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

    public Client(String localDir, String replicaDir, String requestDir, String name, String ip, String server) throws NodeNotRegisteredException {
        try {
            this.serverIp = InetAddress.getByName(server);
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
        this.name = name;
        this.localDir = localDir;
        this.replicaDir = replicaDir;
        this.requestDir = requestDir;
        restClient = new RestClient(serverIp.toString().substring(1));
        discovery();
        register(this.name, this.ip.toString().substring(1));               // TODO valt weg door boostrap?!
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
            publisher.multicast("end");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void bootstrap() {

    }

    public void shutdown() {
        restClient.delete("unregister?name=" + name);

        // TODO let others know to change their prevNode or nextNode
        // TODO relocate hosted files that were on the node
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

    public void run() {
        discovery();
        bootstrap();

        boolean quit = false;

        while (!quit) {

        }


        shutdown();
    }
}