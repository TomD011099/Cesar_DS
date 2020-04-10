package Client;

import java.util.Scanner;

/**
 * Client
 */
public class Client {

    private static RestClient client;

    public static void main(String[] args) {

        if (args.length != 1) {
            System.out.println("Wrong amount of arguments!");
            System.out.println("Should be: <server-ip>");
            return;
        }

        Scanner sc = new Scanner(System.in);
        client = new RestClient(args[0]);
        String action;
        int id, amount;

        // The client can give commands to execute, the program is stopped by pressing x
        do {
            System.out.println("\n\nGive an action (create, deposit, withdraw, balance, delete) | type x to stop");
            action = sc.nextLine();

            switch (action) {
                case "create":
                    System.out.println("Give your name:");
                    create(sc.nextLine());
                    break;
                case "deposit":
                    System.out.println("Give account id:");
                    id = sc.nextInt();
                    System.out.println("Give the amount you want to deposit:");
                    amount = sc.nextInt();
                    deposit(id, amount);
                    break;
                case "withdraw":
                    System.out.println("Give the account id:");
                    id = sc.nextInt();
                    System.out.println("Give the amount you want to withdraw:");
                    amount = sc.nextInt();
                    withdraw(id, amount);
                    break;
                case "balance":
                    System.out.println("Give the account id:");
                    balance(sc.nextInt());
                    break;
                case "delete":
                    System.out.println("Give the account id to delete:");
                    id = sc.nextInt();
                    delete(id);
                    break;
                case "x":
                    System.out.println("Stop the banking client");
                    break;
                default:
                    System.out.println(action + " not known! Give a valid command!");
                    break;
            }
        } while (!action.equals("x"));
    }

    static private void create(String name) {
        System.out.println("Bank created with id: " + client.post("create?name=" + name, null));
    }

    static private void deposit(int id, int amount) {
        if (client.put("deposit?id=" + id + "&amount=" + amount, null).equals("true"))
            System.out.println("€" +  amount + " deposited");
        else
            System.out.println("Invalid amount!");
    }

    static private void withdraw(int id, int amount) {
        if (client.put("withdraw?id=" + id + "&amount=" + amount, null).equals("true"))
            System.out.println("€" +  amount + " withdrawn");
        else
            System.out.println("Invalid amount!");
    }

    static private void balance(int id) {
        System.out.println("Balance: €" + client.get("balance?id=" + id));
    }

    static private void delete(int id) {
        client.delete("delete?id=" + id);
    }
}
