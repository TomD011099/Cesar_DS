package Client;

import jade.core.AID;
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
}
