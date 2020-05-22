package Client.Threads;

import Client.Client;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/**
 * Thread that checks the local files to see if there are any new, changed or deleted files
 */
public class UpdateThread implements Runnable {
    private Path localPath;             //The absoulte path to the directory that has to be checked
    private Client client;              //The instance of client to invoke methods
    private volatile boolean stop;      //Stop the thread
    private WatchService service;       //The service that'll watch the files

    /**
     * Constructor
     *
     * @param client    The instance of client to invoke methods
     * @param localPath The absoulte path to the directory that has to be checked
     */
    public UpdateThread(Client client, String localPath) {
        this.client = client;
        this.localPath = Paths.get(localPath);
        this.stop = false;
        try {
            this.service = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Run the thread
     */
    @Override
    public void run() {
        try {
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

            } while (watchKey.reset() && !stop);
        } catch (ClosedWatchServiceException e) {
            if (!stop) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Update Thread ended");
    }

    public void stop() {
        try {
            stop = true;
            service.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
