package Client.UI;

import Client.Client;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.net.UnknownHostException;
import java.util.HashSet;


public class ClientUI extends Application {

    private Stage window;
    private Client client = null;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        client = ClientSetupBox.display(primaryStage);
        try {
            client.runGraphic();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        window = primaryStage;
        window.setTitle("System Y Client");

        BorderPane layout = new BorderPane();

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem exit = new MenuItem("Exit");
        exit.setOnAction(e -> {
            closeProgram();
        });
        MenuItem settings = new MenuItem("Settings");
        settings.setOnAction(e -> {
            //TODO open settings
        });
        fileMenu.getItems().addAll(settings, exit);
        menuBar.getMenus().add(fileMenu);

        /*Menu connectionMenu = new Menu("Connections");
        MenuItem connect = new MenuItem("Connect");
        connect.setOnAction(e -> {
            //TODO set up connection
        });
        MenuItem disconnect = new MenuItem("Disconnect");
        disconnect.setOnAction(e -> {
            client.stopGraphic();
        });
        connectionMenu.getItems().addAll(connect, disconnect);
        menuBar.getMenus().add(connectionMenu);*/

        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

        layout.setTop(menuBar);
        HashSet<String> testSet = new HashSet<>();
        for (int i = 0; i < 6; i++) {
            testSet.add("File" + (i + 1));
        }
        FileTreePane localFiles = new FileTreePane("Local Files", testSet);
        layout.setLeft(localFiles.getPane());
        //TODO File request buttons and other stuff

        Scene scene = new Scene(layout, 1600, 900);
        window.setScene(scene);
        window.show();
    }

    private void closeProgram() {
        Boolean answer = ConfirmBox.display("Exit", "Are you sure you want to exit?");
        if (answer) {
            client.stopGraphic();
            window.close();
        }
    }
}
