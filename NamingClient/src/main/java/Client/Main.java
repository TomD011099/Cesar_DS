package Client;

import java.net.UnknownHostException;

public class Main {

    public static void main(String[] args) {

        if (args.length != 5) {
            System.out.println("Not the right amount of args");
            System.out.println("Should be: <name> <ip-address> <server-ip>");
            return;
        }

        String name = args[0];
        String ip = args[1];
        String serverIp = args[2];
        String nextIp = args[3];
        String prevIp = args[4];
        Client client;
        try {
            client = new Client("./", "./", "./", name, ip, serverIp, nextIp, prevIp);
        } catch (NodeNotRegisteredException e) {
            System.err.println(e.getMessage());
            return;
        }

        try {
            client.run();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
