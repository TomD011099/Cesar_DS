package Client;

import java.util.Scanner;

public class Client {
    private static RestClient restClient;

    public static void main(String[] args) {
        if (args.length != 1) {
            return;
        }

        Scanner scanner = new Scanner(System.in);
        restClient = new RestClient(args[0]);

        String command;

        do {
            System.out.print("\nGive the command: (help for help): ");
            command = scanner.nextLine();
            String[] parsed = command.split(" ");

            switch (parsed[0]) {
                case "help":
                    System.out.println("Help:\n" +
                            "\tcreateBank [name]\n" +
                            "\tcreateCustomer [bankName] [customerName]\n" +
                            "\tbalance [bankName] [customerId]\n" +
                            "\twithdraw [bankName] [customerId] [amount]\n" +
                            "\tdeposit [bankName] [customerId] [amount]\n" +
                            "\tremoveCustomer [bankName] [customerId]\n" +
                            "\tremoveBank [bankName]\n" +
                            "\t exit");
                    break;
                case "createBank":
                    if (parsed.length == 2) {
                        createBank(parsed[1]);
                    } else
                        err();
                    break;
                case "createCustomer":
                    if (parsed.length == 3) {
                        createCustomer(parsed[1], parsed[2]);
                    } else
                        err();
                    break;
                case "balance":
                    if (parsed.length == 3) {
                        try {
                            balance(parsed[1], Integer.parseInt(parsed[2]));
                        } catch (NumberFormatException e) {
                            err();
                        }
                    } else
                        err();
                    break;
                case "withdraw":
                    if (parsed.length == 4) {
                        try {
                            withdraw(parsed[1], Integer.parseInt(parsed[2]), Integer.parseInt(parsed[3]));
                        } catch (NumberFormatException e) {
                            err();
                        }
                    } else
                        err();
                    break;
                case "deposit":
                    if (parsed.length == 4) {
                        try {
                            deposit(parsed[1], Integer.parseInt(parsed[2]), Integer.parseInt(parsed[3]));
                        } catch (NumberFormatException e) {
                            err();
                        }
                    } else
                        err();
                    break;
                case "removeCustomer":
                    if (parsed.length == 3) {
                        try {
                            removeAccount(parsed[1], Integer.parseInt(parsed[2]));
                        } catch (NumberFormatException e) {
                            err();
                        }
                    } else
                        err();
                    break;
                case "removeBank":
                    if (parsed.length == 2) {
                        try {
                            removeBank(parsed[1]);
                        } catch (NumberFormatException e) {
                            err();
                        }
                    }else
                        err();
                    break;
                case "exit":
                    break;
                default:
                    err();
            }

        } while (!command.equals("exit"));
    }

    static private void err() {
        System.out.println("Not a valid command, type help for the list of commands.");
    }

    static private void createBank(String name) {
        int id = Integer.parseInt(restClient.post("createBank?name=" + name, null));
        if (id != -1)
            System.out.println("Bank created with id " + id);
        else
            System.out.println("Bank already exists");
    }

    static private void createCustomer(String bankName, String customerName) {
        System.out.println("Account created with id " + restClient.post("createCustomer?customerName=" + customerName + "&bankName=" + bankName, null));
    }

    static private void balance(String bankName, int customerId) {
        int bal = Integer.parseInt(restClient.get("balance?bankName=" + bankName + "&customerId=" + customerId));
        if (bal != -1) {
            System.out.println("$" + bal);
        } else {
            System.out.println("Invalid bankName/customerId");
        }
    }

    static private void withdraw(String bankName, int accountId, int amount) {
        if (restClient.put("withdraw?bankName=" + bankName + "&customerId=" + accountId + "&amount=" + amount, null).equals("true")) {
            System.out.println("Money successfully withdrawn");
        } else {
            System.out.println("Invalid amount/bankName/customerId");
        }
    }

    static private void deposit(String bankName, int accountId, int amount) {
        if (restClient.put("deposit?bankName=" + bankName + "&customerId=" + accountId + "&amount=" + amount, null).equals("true")) {
            System.out.println("Money successfully deposited");
        } else {
            System.out.println("Invalid amount/bankName/customerId");
        }
    }

    static private void removeAccount(String bankName, int customerId) {
        if (restClient.delete("removeCustomer?bankName=" + bankName + "&customerId=" + customerId).equals("true")) {
            System.out.println("Account successfully removed");
        } else {
            System.out.println("Account ot bank does not exist");
        }
    }

    static private void removeBank(String bankName) {
        if (restClient.delete("removeBank?bankName=" + bankName).equals("true")) {
            System.out.println("Bank successfully removed");
        } else {
            System.out.println("Bank does not exist");
        }
    }
}
