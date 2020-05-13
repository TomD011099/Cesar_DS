package Client;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

public class SynchAgent implements Runnable, Serializable {

    // List of: filename, lock (bool), owner (name)
    private String replicaDir;
    private ArrayList<ArrayList<String>> list;

    SynchAgent(String replicaDir) {
        list = new ArrayList<>();
        this.replicaDir = replicaDir;
    }

    @Override
    public void run() {

    }

    public String getListOfFilesToString() {
        String string = "";
        for (ArrayList<String> column : list) {
            for (String row : column) {
                string = string + " " + row;
            }
        }
        return string;
    }

    private void updateFiles() {
        File folder = new File(replicaDir);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            ArrayList<String> subList = new ArrayList<>();
            subList.add(file.getName());              // Add the name
            subList.add("false");                     // Add lock or not
            list.add(subList);
        }
    }
}
