package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class MulticastReceiver implements Runnable {
    protected MulticastSocket socket = null;
    private volatile String nodeName = null;
    private volatile InetAddress clientAddr = null;
    private Server server;

    MulticastReceiver(Server server) {
        this.server = server;
    }

    public void run() {
        try {
            socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            while (true) {
                byte[] receive = new byte[65535];
                DatagramPacket nodeInfoPacket = new DatagramPacket(receive, receive.length);
                socket.receive(nodeInfoPacket);
                String strNodeName = new String(nodeInfoPacket.getData());
                nodeName = new String(strNodeName);
                System.out.println("Node " + nodeName + " detected.");
                clientAddr = nodeInfoPacket.getAddress();
                server.registerNode(strNodeName, clientAddr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}