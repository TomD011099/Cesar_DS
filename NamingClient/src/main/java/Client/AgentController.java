package Client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

@Component
@RestController
public class AgentController implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        main(args);
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Not the right amount of args");
            System.out.println("Should be: <name> <ip-address>");
            return;
        }

        String name = args[0];
        String ip = args[1];
        Client client;

        System.out.println("Andere main runned");
        System.out.println("Parameters zijn: " + name + "  " + ip);

        try {
            client = new Client("/home/pi/local/","/home/pi/remote/", "/home/pi/request/", name, ip);

            /*SynchAgent synchAgent = new SynchAgent("/home/pi/remote/", client);
            Thread synchAgentThread = new Thread(synchAgent);
            synchAgentThread.start();*/

            client.run();
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
    }

    @GetMapping("/synchList")
    public String getSynchList() {
        return "Hello World! synchlist";
    }
}
