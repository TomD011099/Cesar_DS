package Client.Threads;

import Client.Client;
import Client.Util.Ports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class DiscoveryThread extends Thread {
    private Client client;
    private ServerSocket serverSocket;

    public DiscoveryThread(Client client) throws IOException {
        serverSocket = new ServerSocket(Ports.discoveryPort);
        this.client = client;
    }

    @Override
    public void run() {
        try {
            // Get the response of the number of nodes
            Socket socket = serverSocket.accept();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            client.discoveryResponse(Integer.parseInt(bufferedReader.readLine()), socket.getInetAddress());
            bufferedReader.close();
            socket.close();
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
