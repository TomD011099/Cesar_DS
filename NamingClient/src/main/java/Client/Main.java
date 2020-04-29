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
            client = new Client("C:/Users/tomde/OneDrive - uantwerpen/School/Erasmus", "./", "./", name, ip);
            client.run();
        } catch (NodeNotRegisteredException | UnknownHostException e) {
            System.err.println(e.getMessage());
        }
    }
}
