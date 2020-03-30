import java.io.*;
import java.net.InetAddress;
import java.util.*;

public class Server {
    private HashMap<Integer, InetAddress> map;
    private String mapPath;

    public Server(String mapPath) {
        this.mapPath = mapPath;
        map = new HashMap<Integer, InetAddress>();
        loadMap();
    }

    private void registerNode(String name, InetAddress ip) {
        int id = getId(name);

        if (map.containsKey(id)) {
            System.err.println("Node with name " + name + " already exists.");
            // TODO make sure Server returns error to node
        } else {
            map.put(id, ip);
            saveMap();
        }
        // TODO make sure existing files are moved to correct node --> not yet
        // TODO make replicas of new files on the new node
    }

    private void unregisterNode(String name) {
        int id = getId(name);

        if (map.containsKey(id)) {
            map.remove(id);
            saveMap();
        } else {
            System.err.println("Node with name " + name + " not found.");
            // TODO make sure Server returns error to node
        }
        // TODO relocate hosted files that were on the node
    }

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

    private void loadMap() {
        Properties properties = new Properties();

        File file = new File(mapPath);

        if (file.exists()) {
            try {
                properties.loadFromXML(new FileInputStream(mapPath));

                Set<String> keys = properties.stringPropertyNames();
                for (String key: keys) {
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

    private InetAddress fileLocation(String fileName) {
        int id = getId(fileName);
        int closestId = Collections.max(map.keySet());
        int closest = closestId - id;

        for (Integer i : map.keySet()) {
            if (id > i) {
                if (id - i <= closest) {
                    closest = id - i;
                    closestId = i;
                }
            }
        }

        return map.get(closestId);
    }

    private int getId(String name) {
        int id = new CesarString(name).hashCode();
        System.out.println(name + "\t id = " + id);
        return id;
    }

    private void clearMap() {
        map.clear();
    }

    public void run() {
        boolean quit = false;

        while (!quit) {

        }
    }
}
