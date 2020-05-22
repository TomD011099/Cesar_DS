package Client.Threads;

import Client.Client;

import java.nio.file.*;
import java.util.*;

/**
 * Thread that checks the local files to see if there are any new, changed or deleted files
 */
public class UpdateThread extends Thread {
    private Path localPath;     //The absoulte path to the directory that has to be checked
    private Client client;      //The instance of client to invoke methods

    /**
     * Constructor
     *
     * @param client    The instance of client to invoke methods
     * @param localPath The absoulte path to the directory that has to be checked
     */
    public UpdateThread(Client client, String localPath) {
        this.client = client;
        this.localPath = Paths.get(localPath);
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try (WatchService service = FileSystems.getDefault().newWatchService()) {
            Map<WatchKey, Path> keyMap = new HashMap<>();
            keyMap.put(localPath.register(service, StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY), localPath);
            WatchKey watchKey;
            do {
                watchKey = service.take();

                for (WatchEvent<?> event : watchKey.pollEvents()) {
                    String eventName = event.kind().name();
                    Path path = (Path) event.context();


                    switch (eventName) {
                        case "ENTRY_CREATE":
                            client.localFileCreated(path.toString());
                            break;
                        case "ENTRY_DELETE":
                            client.localFileDeleted(path.toString());
                            break;
                        case "ENTRY_MODIFY":
                            client.localFileModified(path.toString());
                            break;
                        default:
                            break;
                    }
                }

            } while (watchKey.reset());
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println(Thread.currentThread().getName() + " Ended. (Update)");
    }
}
