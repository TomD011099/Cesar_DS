package Server;

import Server.Threads.*;
import Server.Util.*;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

/**
 * This is the server class, it'll tell the clients where to find/store their files
 */
public class Server {
    private HashMap<Integer, InetAddress> map;      //A map of clients in the network
    private String mapPath;                         //The location of the map
    private MulticastReceiver multicastReceiver;    //A multicastReceiver to detect multicastmessages
    private Thread receiverThread;                  //The thread that runs multicastReceiver

    /**
     * Constructor
     *
     * @param mapPath The location where the map will be stored
     */
    public Server(String mapPath) {
        multicastReceiver = new MulticastReceiver(this);
        receiverThread = new Thread(multicastReceiver);
        receiverThread.start();
        this.mapPath = mapPath;
        map = new HashMap<Integer, InetAddress>();
        loadMap();
    }

    /**
     * A new node has joined the network
     *
     * @param name The name of the node that joined
     * @param ip   The ip address of the node that joined
     * @return <ul><li>-1: The map already has that ID</li><li>1: Registered successfully</li></ul>
     */
    public int registerNode(String name, InetAddress ip) {
        int id = getId(name);

        if (map.containsKey(id)) {
            return -1;
        } else {
            map.put(id, ip);
            saveMap();
            return 1;
        }
    }

    /**
     * Called by MulticastReceiver, sends the amount of nodes in the network back
     *
     * @param nodeName The name of the node that joined
     * @param ip       The ip address of the node that joined
     */
    public void handleMulticastMessage(String nodeName, InetAddress ip) {
        // Reply with the number of nodes in the network
        sendNumberOfNodes(ip, (registerNode(nodeName, ip) == 1));

        // Register the node
        registerNode(nodeName, ip);
    }

    /**
     * Send the number of nodes to the new node
     *
     * @param ip The ip address of the node that joined
     */
    private void sendNumberOfNodes(InetAddress ip, boolean ok) {
        try {
            Socket socket = new Socket(ip, Ports.discoveryPort);

            // Create a writer to write to the socket
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            if (ok)
                writer.println(map.size() - 1);
            else
                writer.println(-1);

            writer.close();
            socket.close();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Unregister a node that is going to leave the network
     *
     * @param name The name of the node
     */
    public void unregisterNode(String name) {
        int id = getId(name);

        if (map.containsKey(id)) {
            map.remove(id);
            saveMap();
        } else {
            System.err.println("Node with name " + name + " not found.");
        }
    }

    /**
     * Save the map on the hard drive
     */
    private void saveMap() {
        try {
            Properties properties = new Properties();

            for (Map.Entry<Integer, InetAddress> pair : map.entrySet()) {
                String strIp = pair.getValue().toString();
                properties.put(pair.getKey().toString(), strIp.substring(1));
            }

            FileOutputStream fos = new FileOutputStream(mapPath);

            properties.storeToXML(fos, "Map of nodes");

            fos.close();
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load the map from the hard drive
     */
    private void loadMap() {
        Properties properties = new Properties();

        File file = new File(mapPath);

        if (file.exists()) {
            try {
                properties.loadFromXML(new FileInputStream(mapPath));

                Set<String> keys = properties.stringPropertyNames();
                for (String key : keys) {
                    int id = Integer.parseInt(key);
                    InetAddress ip = InetAddress.getByName(properties.getProperty(key));
                    map.put(id, ip);
                }
            } catch (FileNotFoundException e) {
                System.err.println(e.getMessage());
            } catch (IOException e) {
                System.err.println(e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculate where a file is located
     *
     * @param fileName The file to check
     * @return The ip address of the node that has the file
     */
    public InetAddress fileLocation(String fileName) {
        int id = getId(fileName);
        int closestId = Collections.min(map.keySet());
        int closest = id - closestId;

        if (closest < 0) {
            closestId = Collections.max(map.keySet());
        } else {
            for (Integer i : map.keySet()) {
                if (id > i) {
                    if (id - i <= closest) {
                        closest = id - i;
                        closestId = i;
                    }
                }
            }
        }

        return map.get(closestId);
    }

    /**
     * Get the id of a name
     *
     * @param name The name of the file
     * @return The ID of the file
     */
    private int getId(String name) {
        int id = new CesarString(name).hashCode();
        System.out.println(name + "\t id = " + id);
        return id;
    }

    /**
     * Clear the map
     */
    public void clearMap() {
        map.clear();
        saveMap();
    }
}