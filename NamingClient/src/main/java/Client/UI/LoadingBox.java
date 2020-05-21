package Client.UI;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class LoadingBox {
    private String title;
    private String message;
    private Stage window;

    public LoadingBox(String title, String message) {
        this.title = title;
        this.message = message;
        window = new Stage();
    }

    public void display() {
        window.initModality(Modality.APPLICATION_MODAL);
        window.initStyle(StageStyle.UNDECORATED);
        window.setTitle(title);
        window.setMinWidth(250);


        Label label = new Label();
        label.setText(message);

        ProgressBar pb = new ProgressBar(-1.0);
        VBox layout = new VBox(10);
        layout.getChildren().addAll(label, pb);
        layout.setAlignment(Pos.CENTER);

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }

    public void close(){
        window.close();
    }
}
