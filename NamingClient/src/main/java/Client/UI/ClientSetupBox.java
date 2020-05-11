package Client.UI;

import Client.Client;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;


public class ClientSetupBox {

    private static Client client;

    public static Client display(Stage primaryStage) {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Initialise client");
        window.setMinWidth(250);

        Label hostNameLabel = new Label();
        hostNameLabel.setText("Name of client:");
        TextField nameField = new TextField();

        Label hostIpLabel = new Label();
        hostIpLabel.setText("Ip address:");
        TextField ipField = new TextField();

        Label localFilesLabel = new Label();
        localFilesLabel.setText("Directory for local files:");
        TextField localFilesField = new TextField();
        Button localBrowse = new Button("Browse");
        localBrowse.setOnAction(e -> {
            DirectoryBrowser browser = new DirectoryBrowser(primaryStage);
            localFilesField.setText(browser.select());
        });

        Label replicatedFilesLabel = new Label();
        replicatedFilesLabel.setText("Directory for replicated files:");
        TextField replicatedFilesField = new TextField();
        Button replicaBrowse = new Button("Browse");
        replicaBrowse.setOnAction(e -> {
            DirectoryBrowser browser = new DirectoryBrowser(primaryStage);
            replicatedFilesField.setText(browser.select());
        });

        Label requestedFilesLabel = new Label();
        requestedFilesLabel.setText("Directory for requested files:");
        TextField requestedFilesField = new TextField();
        Button requestBrowse = new Button("Browse");
        requestBrowse.setOnAction(e -> {
            DirectoryBrowser browser = new DirectoryBrowser(primaryStage);
            requestedFilesField.setText(browser.select());
        });

        Button confirmButton = new Button("Ok");
        confirmButton.setOnAction(e -> {
            if (isValid(nameField.getText(), ipField.getText(), localFilesField.getText(), replicatedFilesField.getText(), requestedFilesField.getText())) {
                client = new Client(localFilesField.getText().replace('\\', '/'), replicatedFilesField.getText().replace('\\', '/'), replicatedFilesField.getText().replace('\\', '/'), nameField.getText(), ipField.getText());
                window.close();
            } else {
                AlertBox.display("Error", "One or more of the given parameters are not valid");
            }
        });
        window.setOnCloseRequest(e -> {
            e.consume();
            client = null;
            window.close();
        });

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        gridPane.add(hostNameLabel, 0, 0);
        gridPane.add(nameField, 1, 0);

        gridPane.add(hostIpLabel, 0, 1);
        gridPane.add(ipField, 1, 1);

        gridPane.add(localFilesLabel, 0, 2);
        gridPane.add(localFilesField, 1, 2);
        gridPane.add(localBrowse, 2, 2);

        gridPane.add(replicatedFilesLabel, 0, 3);
        gridPane.add(replicatedFilesField, 1, 3);
        gridPane.add(replicaBrowse, 2, 3);

        gridPane.add(requestedFilesLabel, 0, 4);
        gridPane.add(requestedFilesField, 1, 4);
        gridPane.add(requestBrowse, 2, 4);

        gridPane.add(confirmButton, 2, 5);

        Scene scene = new Scene(gridPane);
        window.setScene(scene);
        window.showAndWait();
        return client;
    }

    private static Boolean isValid(String fileName, String ip, String localDir, String replicaDir, String requestedDir) {
        boolean answer = true;
        File localFiles = new File(localDir);
        File replicaFiles = new File(replicaDir);
        File requestedFiles = new File(requestedDir);
        if (fileName == null || !localFiles.exists() || !replicaFiles.exists() || !requestedFiles.exists()) {
            answer = false;
        }
        try {
            if (ip == null || ip.isEmpty()) {
                answer = false;
            }

            String[] parts = ip.split("\\.");
            if (parts.length != 4) {
                answer = false;
            }

            for (String s : parts) {
                int i = Integer.parseInt(s);
                if ((i < 0) || (i > 255)) {
                    answer = false;
                }
            }
            if (ip.endsWith(".")) {
                answer = false;
            }
        } catch (NumberFormatException e) {
            answer = false;
        }
        return answer;
    }

}
