package Client.UI;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

import java.util.HashSet;

public class FileTreePane {
    private final StackPane pane;

    public FileTreePane(String path, HashSet<String> files, Label selectedFile){
        TreeItem<String> rootItem = new TreeItem<>(path);
        rootItem.setExpanded(true);
        for (String file : files){
            TreeItem<String> item =  new TreeItem<>(file);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selectedFile.setText(newValue.getValue()));
        DragResizer.makeResizable(tree);
        pane = new StackPane();
        pane.getChildren().add(tree);
    }

    public FileTreePane(String path, HashSet<String> files){
        TreeItem<String> rootItem = new TreeItem<>(path);
        rootItem.setExpanded(true);
        for (String file : files){
            TreeItem<String> item =  new TreeItem<>(file);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        DragResizer.makeResizable(tree);
        pane = new StackPane();
        pane.getChildren().add(tree);
    }

    public StackPane getPane(){
        return pane;
    }
}
