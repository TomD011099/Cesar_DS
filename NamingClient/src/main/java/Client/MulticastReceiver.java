package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class MulticastReceiver implements Runnable {
    private Client client;
    private volatile boolean quit;

    MulticastReceiver(Client client) {
        this.client = client;
        this.quit = false;
    }

    public void run() {
        try {
            MulticastSocket socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            while (!quit) {
                byte[] receive = new byte[65535];
                DatagramPacket nodeInfoPacket = new DatagramPacket(receive, receive.length);
                socket.receive(nodeInfoPacket);
                String strNodeName = new String(nodeInfoPacket.getData());
                System.out.println("Node " + strNodeName + " detected.");
                client.handleMulticastMessage(strNodeName, nodeInfoPacket.getAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        quit = true;
    }
}