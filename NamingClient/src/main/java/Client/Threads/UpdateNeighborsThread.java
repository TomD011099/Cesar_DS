package Client.Threads;

import Client.Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

public class UpdateNeighborsThread extends Thread {
    protected Socket socket;
    private Client client;

    public UpdateNeighborsThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            // Create a reader to read from the socket
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String in = reader.readLine();
            String[] parsed = in.split(" ");
            if (parsed[0].equals("prev")) {
                client.setPrevNode(InetAddress.getByName(parsed[1].substring(1)));
                System.out.println("prevNode updated to: " + client.getPrevNode());
            } else if (parsed[0].equals("next")) {
                client.setNextNode(InetAddress.getByName(parsed[1].substring(1)));
                System.out.println("nextNode updated to: " + client.getNextNode());
            }

            reader.close();
            socket.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
