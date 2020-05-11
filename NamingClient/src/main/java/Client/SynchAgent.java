package Client;

import java.io.Serializable;
import java.util.ArrayList;

public class SynchAgent implements Runnable, Serializable {

    // List of: filename, lock (bool), owner (bool)
    private ArrayList<ArrayList<String>> list;

    SynchAgent() {
        list = new ArrayList<>();
    }

    @Override
    public void run() {

    }
}


/*import jade.core.AID;
import jade.core.Agent;
import java.io.Serializable;

public class SynchAgent extends Agent implements Serializable, Runnable {

    String hostname;

    SynchAgent(String hostname) {
        this.hostname = hostname;
        AID id = new AID(hostname, AID.ISLOCALNAME);
    }

    @Override
    protected void setup(){
        System.out.println("Hello! Buyer-agent "+getAID().getName()+ "is ready.");
    }
}*/
