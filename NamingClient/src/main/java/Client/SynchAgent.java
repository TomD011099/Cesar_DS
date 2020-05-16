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

    SynchAgent(String replicaDir) {
        list = new ArrayList<>();
        this.replicaDir = replicaDir;
        this.replicaPath = Paths.get(replicaDir);
        updateFiles();
    }

    @Override
    public void run() {
        while (true) {
            // Check in remote map if a file is added or deleted
            if (checkForFileChanges()) {
                // TODO update the client's list
            }

            try {
                // Delay 5 seconds
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

    private boolean checkForFileChanges() {
        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            keyMap.put(replicaPath.register(service, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE), replicaPath);
            WatchKey watchKey;
            do {
                watchKey = service.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String eventName = event.kind().name();
                    Path path = (Path) event.context();

                    ArrayList<String> subList = new ArrayList<>();
                    if(eventName.contains("ENTRY_CREATE")) {
                        subList.add(path.toString());              // Add the name
                        subList.add("false");                      // Add lock or not
                        list.add(subList);                         // Add file to the list
                        System.out.println("File created");
                        System.out.println(list);
                        return true;
                    } else if (eventName.contains("ENTRY_DELETE")) {
                        subList.add(path.toString());              // Add the name
                        subList.add("false");                      // Add lock or not
                        list.remove(subList);                      // Remove the object from the list
                        System.out.println("File deleted");
                        System.out.println(list);
                        return true;
                    }
                }
                return false;
            } while (watchKey.reset());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void updateFiles() {
        File folder = new File(replicaDir);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            ArrayList<String> subList = new ArrayList<>();
            subList.add(file.getName());              // Add the name
            subList.add("false");                     // Add lock or not
            list.add(subList);
        }
    }
}
