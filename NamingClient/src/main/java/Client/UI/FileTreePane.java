package Client.UI;

import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.util.HashSet;

public class FileTreePane {
    private final StackPane pane;
    private final String path;

    public FileTreePane(String path, String name, HashSet<String> files, Label selectedFile) {
        TreeItem<String> rootItem = new TreeItem<>(name);
        rootItem.setExpanded(true);
        for (String file : files) {
            TreeItem<String> item = new TreeItem<>(file);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        tree.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> selectedFile.setText(path+newValue.getValue()));
        DragResizer.makeResizable(tree);
        pane = new StackPane();
        pane.getChildren().add(tree);
        this.path = path;
    }

    public FileTreePane(String path, String name, HashSet<String> files) {
        TreeItem<String> rootItem = new TreeItem<>(name);
        rootItem.setExpanded(true);
        for (String file : files) {
            TreeItem<String> item = new TreeItem<>(file);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        DragResizer.makeResizable(tree);
        pane = new StackPane();
        pane.getChildren().add(tree);
        this.path = path;
    }

    public StackPane getPane() {
        return pane;
    }

    public String getPath() {
        return path;
    }
}
