package Client.Threads;

import Client.Client;

import java.nio.file.*;
import java.util.*;

public class UpdateThread extends Thread {
    private Path localPath;
    private Client client;

    public UpdateThread(Client client, String localPath) {
        this.client = client;
        this.localPath = Paths.get(localPath);
    }

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
    }
}
