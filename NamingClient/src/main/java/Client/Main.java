package Client;

import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Not the right amount of args");
            System.out.println("Should be: <name> <ip-address>");
            return;
        }

        String name = args[0];
        String ip = args[1];
        Client client;
        try {
            client = new Client("/home/pi/local/","/home/pi/remote/", "/home/pi/request/", name, ip);
            client.run();
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
    }
}
