package Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class MulticastReceiver implements Runnable {
    protected MulticastSocket socket = null;
    private volatile String nodeName = null;
    private volatile InetAddress clientAddr = null;
    private boolean received = false;

    public void run() {
        try {
            socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            while (true) {
                received = false;
                // Read the path to the file that has to be copied
                byte[] receive = new byte[65535];
                DatagramPacket nodeInfoPacket = new DatagramPacket(receive, receive.length);
                socket.receive(nodeInfoPacket);
                String strNodeName = new String(nodeInfoPacket.getData());
                if (strNodeName.contains("end")) {
                    break;
                } else {
                    nodeName = new String(strNodeName);
                    System.out.println("Node " + nodeName + " detected.");
                    clientAddr = nodeInfoPacket.getAddress();
                }
            }
            received = true;
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            received = false;
        }
    }

    public String getData() {
        received = false;
        return nodeName +  " " + clientAddr.toString();
    }

    public boolean hasReceived() {
        return received;
    }
}