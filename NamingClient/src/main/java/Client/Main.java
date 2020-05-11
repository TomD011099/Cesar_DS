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
            client = new Client("D:/My Documents/UA/3e Bach industrieel Ingenieur EI/6-Distributed Systems/Test","D:/My Documents/UA/3e Bach industrieel Ingenieur EI/6-Distributed Systems/Test", "D:/My Documents/UA/3e Bach industrieel Ingenieur EI/6-Distributed Systems/Test", name, ip);
            client.run();
        } catch (UnknownHostException e) {
            System.err.println(e.getMessage());
        }
    }
}
