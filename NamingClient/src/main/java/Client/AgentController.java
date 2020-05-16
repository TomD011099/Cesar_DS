package Client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AgentController {

    private SynchAgent synchAgent = new SynchAgent("/home/pi/remote/");

    AgentController() {
        Thread synchAgentThread = new Thread(synchAgent);
        synchAgentThread.start();
        System.out.println("AgentController maakt dink");
    }

    @GetMapping("/synchList")
    public String getSynchList() {
        return synchAgent.getListOfFilesToString();
    }
}
