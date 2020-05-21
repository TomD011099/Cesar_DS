package Server.Threads;

import Server.Server;
import Server.Util.Ports;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * A thread that will receive the multicastmessages sent by the MulticastPublisher
 */
public class MulticastReceiver implements Runnable {
    private Server server;  //The server to invoke methods

    /**
     * The constructor
     *
     * @param server The server instance
     */
    public MulticastReceiver(Server server) {
        this.server = server;
    }

    /**
     * Run the thread
     */
    public void run() {
        try {
            //Create a socket and join the group
            MulticastSocket socket = new MulticastSocket(Ports.multicastPort);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);

            //Receive the packets one by one and let the client handle the messages as they come in
            while (true) {
                byte[] receive = new byte[65535];
                DatagramPacket nodeInfoPacket = new DatagramPacket(receive, receive.length);
                socket.receive(nodeInfoPacket);
                String strNodeName = new String(nodeInfoPacket.getData());
                System.out.println("Node " + strNodeName + " detected.");
                server.handleMulticastMessage(strNodeName, nodeInfoPacket.getAddress());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}