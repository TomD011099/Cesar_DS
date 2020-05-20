package Client.UI;

import Client.Client;
import Client.Threads.RequestFileThread;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Objects;


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
        fileMenu.getItems().addAll(exit);
        menuBar.getMenus().add(fileMenu);

        window.setOnCloseRequest(e -> {
            e.consume();
            closeProgram();
        });

        layout.setTop(menuBar);

        draw(primaryStage, layout);

        Scene scene = new Scene(layout, 1600, 900);
        window.setScene(scene);
        window.show();
    }

    private void closeProgram() {
        Boolean answer = ConfirmBox.display("Exit", "Are you sure you want to exit?");
        if (answer) {
            if (client != null) {
                client.stopGraphic();
            }
            window.close();
        }
    }

    private void draw(Stage primaryStage, BorderPane layout) {
        Label selectedFile = new Label();

        HBox filePane = new HBox();
        HashSet<String> localFileSet = new HashSet<>();
        File[] files = new File(client.getLocalDir()).listFiles();
        for (File file : Objects.requireNonNull(files)) {
            String tempName = file.getName();
            localFileSet.add(tempName);
        }
        FileTreePane localFiles = new FileTreePane(client.getLocalDir(), "Local Files", localFileSet, selectedFile);
        HashSet<String> requestedFileSet = new HashSet<>();
        files = new File(client.getRequestDir()).listFiles();
        for (File file : Objects.requireNonNull(files)) {
            String tempName = file.getName();
            requestedFileSet.add(tempName);
        }
        FileTreePane requestedFiles = new FileTreePane(client.getRequestDir(), "Downloaded files", requestedFileSet, selectedFile);
        HashSet<String> replicatedFileSet = new HashSet<>();
        files = new File(client.getReplicaDir()).listFiles();
        for (File file : Objects.requireNonNull(files)) {
            String tempName = file.getName();
            replicatedFileSet.add(tempName);
        }
        FileTreePane replicatedFiles = new FileTreePane(client.getReplicaDir(), "Replicated files", replicatedFileSet);
        filePane.getChildren().addAll(localFiles.getPane(), requestedFiles.getPane(), replicatedFiles.getPane());

        layout.setCenter(filePane);

        FlowPane buttonPane = new FlowPane();
        Button addFileButton = new Button("Add local file");
        addFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            File source = fileChooser.showOpenDialog(primaryStage);
            if (source != null) {
                File dest = new File(client.getLocalDir() + source.getName());
                try {
                    Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    draw(primaryStage, layout);
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            }
        });
        TextField fileNameField = new TextField();
        fileNameField.setOnKeyPressed(ke -> {
            if (ke.getCode().equals(KeyCode.ENTER)) {
                String fileToRequest = fileNameField.getText();
                if (fileToRequest == null) {
                    AlertBox.display("Error", "Insert a filename");
                } else {
                    try {
                        String location = client.requestFileLocation(fileToRequest);
                        Thread requestFileThread = new RequestFileThread(InetAddress.getByName(location.substring(1)), fileToRequest, client.getRequestDir());
                        requestFileThread.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        Label requestFile = new Label();
        requestFile.setText("Request file: ");
        Button deleteButton = new Button("Delete file");
        deleteButton.setOnAction(e -> {
            File toDelete = new File(selectedFile.getText());
            if (toDelete.getAbsolutePath().replace('\\', '/').contains(client.getLocalDir()) || toDelete.getAbsolutePath().replace('\\', '/').contains(client.getRequestDir())) {
                if (toDelete.delete()) {
                    AlertBox.display("Success", "File successfully deleted");
                } else {
                    AlertBox.display("Error", "File could not be deleted");
                }
            } else if (toDelete.getAbsolutePath().replace('\\', '/').contains(client.getReplicaDir())) {
                AlertBox.display("Error", "You can not delete replicated files");
            } else {
                AlertBox.display("Error", "No valid file selected");
            }
            draw(primaryStage, layout);
        });
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            draw(primaryStage, layout);
        });
        buttonPane.getChildren().addAll(addFileButton, requestFile, fileNameField, selectedFile, deleteButton, refreshButton);
        layout.setBottom(buttonPane);
    }
}
