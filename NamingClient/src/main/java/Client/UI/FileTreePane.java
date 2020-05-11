package Client.UI;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

import java.util.HashSet;

public class FileTreePane {
    private StackPane pane;

    public FileTreePane(String path, HashSet<String> files){
        TreeItem<String> rootItem = new TreeItem<>(path);
        rootItem.setExpanded(true);
        for (String file : files){
            TreeItem<String> item =  new TreeItem<>(file);
            rootItem.getChildren().add(item);
        }
        TreeView<String> tree = new TreeView<>(rootItem);
        pane = new StackPane();
        pane.getChildren().add(tree);
    }

    public StackPane getPane(){
        return pane;
    }
}
