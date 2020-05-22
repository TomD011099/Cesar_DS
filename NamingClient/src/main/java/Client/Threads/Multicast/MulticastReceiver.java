package Client.Threads.Multicast;

import Client.Client;
import Client.Util.Ports;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * A thread that will receive the multicastmessages sent by the MulticastPublisher
 */
public class MulticastReceiver implements Runnable {
    private Client client;              //The client to invoke methods
    private volatile boolean quit;      //A bool to end the thread
    private MulticastSocket socket;

    /**
     * The constructor
     *
     * @param client The client instance
     */
    public MulticastReceiver(Client client) {
        this.client = client;
        this.quit = false;
        try {
            this.socket  = new MulticastSocket(Ports.multicastNeighborPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the thread
     */
    public void run() {
        try {
            //Create a socket and join the group
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);

            //Receive the packets one by one and let the client handle the messages as they come in
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

        System.out.println("MulticastReceiver Thread ended");
    }

    /**
     * Stop the thread
     */
    public void stop() {
        try {
            quit = true;
            socket.close();
        } catch (Exception e) {
            e.getMessage();
        }
    }
}