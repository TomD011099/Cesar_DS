package Client.UI;

import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;

public class DirectoryBrowser {

    private final Stage primaryStage;

    public DirectoryBrowser(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public String select() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File dir = directoryChooser.showDialog(primaryStage);

        while (dir == null) {
            AlertBox.display("Error", "Choose a valid path");
            dir = directoryChooser.showDialog(primaryStage);
        }
        String path = dir.getAbsolutePath();
        path = path.replace('\\', '/');
        return path;
    }
}
