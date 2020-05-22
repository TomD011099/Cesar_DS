package Client.Threads;

import Client.Client;
import Client.Util.Ports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Used to receive the amount of nodes in the network and the ip address of the server
 */
//TODO could be in TCPControl
public class DiscoveryThread extends Thread {
    private Client client;                  //An instance of client to invoke methods
    private ServerSocket serverSocket;      //The serversocket to receive communications

    /**
     * Constructor
     *
     * @param client An instance of client to invoke methods
     * @throws IOException If discoveryPort is invalid/in use
     */
    public DiscoveryThread(Client client) throws IOException {
        serverSocket = new ServerSocket(Ports.discoveryPort);
        this.client = client;
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
            // Get the response of the number of nodes
            Socket socket = serverSocket.accept();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            int response = Integer.parseInt(bufferedReader.readLine());

            if (response != -1) {
                client.discoveryResponse(response, socket.getInetAddress(), true);
            } else {
                client.discoveryResponse(response, socket.getInetAddress(), false);
            }

            bufferedReader.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Discovery Thread ended");
    }
}
