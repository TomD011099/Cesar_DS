package Client.UI;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FileNameBox {
    public static void display() {
        Stage window = new Stage();
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("Request File");
        window.setMinWidth(250);
        Label fileNameLabel = new Label();
        fileNameLabel.setText("Name of requested file:");
        TextField fileField = new TextField();
        Button confirmButton = new Button("Ok");
        confirmButton.setOnAction(e -> {
            if (isValid(fileField.getText())) {
                //TODO request file
                window.close();
            } else {
                AlertBox.display("Error", "Please insert file name");
            }
        });
        window.setOnCloseRequest(e->{
            e.consume();
            window.close();
        });

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
        gridPane.add(fileNameLabel, 0, 0);
        gridPane.add(fileField,1,0);
        gridPane.add(confirmButton,1,1);

        Scene scene = new Scene(gridPane);
        window.setScene(scene);
        window.showAndWait();
    }

    private static Boolean isValid(String fileName) {
        boolean answer = true;
        if (fileName == null) {
            answer = false;
        }
        return answer;
    }
}
