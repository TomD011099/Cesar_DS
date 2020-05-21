package Client.Threads.Multicast;

import Client.Util.Ports;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * A class that will send a message via multicast
 */
public class MulticastPublisher {
    /**
     * This method will send a message to the server listening to group 230.0.0.0
     *
     * @param message The message to be sent
     * @throws IOException If something goes wrong (creating socket, parsing the group address, sending the packet)
     */
    public void multicastServer(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, Ports.multicastServerPort);
        socket.send(packet);
        socket.close();
    }

    /**
     * This method will send a message to all nodes listening to group 230.0.0.0
     *
     * @param message The message to be sent
     * @throws IOException If something goes wrong (creating socket, parsing the group address, sending the packet)
     */
    public void multicastNeigbors(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, Ports.multicastNeighborPort);
        socket.send(packet);
        socket.close();
    }
}
