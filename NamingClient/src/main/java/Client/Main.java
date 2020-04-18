package Client;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        if (args.length != 3) {
            System.out.println("Not the right amount of args");
            System.out.println("Should be: <name> <ip-address> <server-ip>");
            return;
        }

        String name = args[0];
        String ip = args[1];
        String serverIp = args[2];
        Client client;
        try {
            client = new Client("./", "./", "./", name, ip, serverIp);
        } catch (NodeNotRegisteredException e) {
            System.err.println(e.getMessage());
            return;
        }

        Scanner sc = new Scanner(System.in);
        String input;

        do {
            System.out.println("\n\nGive the file path you want to access: (press x to stop)");
            input = sc.nextLine();
            if (!input.isEmpty() && !input.equals("x")) {
                String location = client.requestFile(input);
                System.out.println("Location: " + location);
            }
        } while (!input.equals("x"));

        System.out.println("Client stopped!");
        client.shutdown();
    }
}
