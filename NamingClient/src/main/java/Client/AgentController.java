package Client;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.UnknownHostException;

@Component
@RestController
public class AgentController implements CommandLineRunner {

    private static Client client;

    /*
        This makes sure that we can create a Client in AgentController with the arguments
        passed from the CLI
     */
    @Override
    public void run(String... args) {
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

        try {
            client = new Client("/home/pi/local/","/home/pi/remote/", "/home/pi/request/", name, ip);
            client.run();
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
    }

    @GetMapping("/fileList")
    public String getSynchList() {
        return client.listToString();
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
