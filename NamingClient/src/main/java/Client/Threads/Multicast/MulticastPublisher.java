package Client.Threads.Multicast;

import Client.Util.Ports;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class MulticastPublisher {

    public void multicast(String message) throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress group = InetAddress.getByName("230.0.0.0");
        byte[] buf = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buf, buf.length, group, Ports.multicastPort);
        socket.send(packet);
        socket.close();
    }
}
