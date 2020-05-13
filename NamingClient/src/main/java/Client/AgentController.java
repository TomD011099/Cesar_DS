package Client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@RestController
public class AgentController {

    private SynchAgent synchAgent = new SynchAgent("/home/pi/remote/");

    @GetMapping("/synchList")
    public String getSynchList() {
        return synchAgent.getListOfFilesToString();
    }
}
