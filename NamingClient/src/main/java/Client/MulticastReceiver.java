package Client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

public class MulticastReceiver implements Runnable {
    protected MulticastSocket socket = null;
    private volatile CesarString nodeName = null;
    private volatile InetAddress clientAddr = null;
    private boolean running = false;

    public void run() {
        try {
            socket = new MulticastSocket(4446);
            InetAddress group = InetAddress.getByName("230.0.0.0");
            socket.joinGroup(group);
            socket.setSoTimeout(10000);
            while (true) {
                try {
                    running = true;
                    // Read the path to the file that has to be copied
                    byte[] receive = new byte[65535];
                    DatagramPacket nodeInfoPacket = new DatagramPacket(receive, receive.length);
                    socket.receive(nodeInfoPacket);
                    String strNodeName = new String(nodeInfoPacket.getData());
                    if ("end".equals(strNodeName)) {
                        break;
                    }
                    nodeName = new CesarString(strNodeName);
                    System.out.println("Node" + nodeName + " detected.");
                    clientAddr = nodeInfoPacket.getAddress();
                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout reached");
                    running = false;
                    socket.leaveGroup(group);
                    socket.close();
                    break;
                }
            }
            running = false;
            socket.leaveGroup(group);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
            running = false;
        }
    }

    public CesarString getNodeName() {
        return nodeName;
    }

    public InetAddress getInetAddress() {
        return clientAddr;
    }

    public boolean isRunning() {
        return running;
    }
}