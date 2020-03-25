import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Map;

public class Server {
    //TODO change to CesarMap when available
    private Map<Integer, InetAddress> map;
    private String mapPath;

    public Server() {
        loadMap();
    }

    public void registerNode(String name, InetAddress ip) {
        // TODO CHA String name to custom NodeName
        int id = name.hashCode();
        if (map.containsKey(id)) {
            System.err.println("Node with name " + name + " already exists.");
            // TODO make sure Server returns error to node
        } else {
            map.put(id, ip);
            saveMap();
        }
    }

    public void unregisterNode(String name) {
        // TODO CHA String name to custom NodeName
        int id = name.hashCode();
        if (map.containsKey(id)) {
            map.remove(id);
            saveMap();
        } else {
            System.err.println("Node with name " + name + " not found.");
        }
    }

    private void saveMap() {
        try {
            BufferedWriter fileWriter = new BufferedWriter(new FileWriter(mapPath));

            Iterator<Map.Entry<Integer, InetAddress>> it = map.entrySet().iterator();

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }

    }

    private void loadMap() {

    }

    public void run() {

    }
}
