package Client;

import java.io.File;
import java.io.Serializable;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SynchAgent implements Runnable, Serializable {

    // List of: filename, lock (bool), owner (name)
    private String replicaDir;
    private Path replicaPath;
    private ArrayList<ArrayList<String>> list;
    private Client client;

    SynchAgent(Client client, String replicaDir) {
        list = new ArrayList<>();
        this.client = client;
        this.replicaDir = replicaDir;
        this.replicaPath = Paths.get(replicaDir);

        // Get all the files in remote directory at startup
        updateFiles();
    }

    @Override
    public void run() {
        while (true) {
            System.out.println("Check");

            // Check if files are added to the remote directory
            checkForFileChanges();

            // Update our list in client based on the next nodes list
            client.updateList();

            // Delay 5 seconds
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.getMessage();
            }
        }
    }

    public String getListOfFilesToString() {
        String string = "";
        for (ArrayList<String> column : list) {
            for (String row : column) {
                string = string + " " + row;
            }
        }
        return string;
    }

    private void checkForFileChanges() {
        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            keyMap.put(replicaPath.register(service, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE), replicaPath);
            WatchKey watchKey;
            //do {
                watchKey = service.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String eventName = event.kind().name();
                    Path path = (Path) event.context();

                    ArrayList<String> subList = new ArrayList<>();
                    if (eventName.contains("ENTRY_CREATE")) {
                        subList.add(path.toString());                   // Add the name
                        subList.add("false");                           // Add lock or not
                        list.add(subList);                              // Add file to the list
                        client.addFilesInSystem(subList);               // Add the file to the nodes list
                        return;
                    } else if (eventName.contains("ENTRY_DELETE")) {
                        subList.add(path.toString());                   // Add the name
                        subList.add("false");                           // Add lock or not
                        list.remove(subList);                           // Remove the file from the list
                        client.removeFilesInSystem(subList);            // Remove the file from the nodes list
                        return;
                    }
                }
            //} while (watchKey.reset());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateFiles() {
        File folder = new File(replicaDir);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            ArrayList<String> subList = new ArrayList<>();
            subList.add(file.getName());                // Add the name
            subList.add("false");                       // Add lock or not
            list.add(subList);                          // Add file to the list
            client.addFilesInSystem(subList);           // Add the file to the nodes list
        }
    }
}
