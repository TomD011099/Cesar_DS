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
                            "\tcreateAccount [bankName] [accountName]\n" +
                            "\tbalance [bankName] [accountId]\n" +
                            "\twithdraw [bankName] [accountId] [amount]\n" +
                            "\tdeposit [bankName] [accountId] [amount]\n" +
                            "\tremoveAccount [bankName] [accountId]\n" +
                            "\tremoveBank [bankName]\n" +
                            "\t exit");
                    break;
                case "createBank":
                    if (parsed.length == 2) {
                        createBank(parsed[1]);
                    } else
                        err();
                    break;
                case "createAccount":
                    if (parsed.length == 3) {
                        createAccount(parsed[1], parsed[2]);
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
                case "removeAccount":
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

    static private void createAccount(String bankName, String accountName) {
        System.out.println("Account created with id " + restClient.post("createAccount?accountName=" + accountName + "&bankName=" + bankName, null));
    }

    static private void balance(String bankName, int accountId) {
        int bal = Integer.parseInt(restClient.get("balance?bankName=" + bankName + "&accountId=" + accountId));
        if (bal != -1) {
            System.out.println("$" + bal);
        } else {
            System.out.println("Invalid bankName/accountId");
        }
    }

    static private void withdraw(String bankName, int accountId, int amount) {
        if (restClient.put("withdraw?bankName=" + bankName + "&accountId=" + accountId + "&amount=" + amount, null).equals("true")) {
            System.out.println("Money successfully withdrawn");
        } else {
            System.out.println("Invalid amount/bankName/accountId");
        }
    }

    static private void deposit(String bankName, int accountId, int amount) {
        if (restClient.put("deposit?bankName=" + bankName + "&accountId=" + accountId + "&amount=" + amount, null).equals("true")) {
            System.out.println("Money successfully deposited");
        } else {
            System.out.println("Invalid amount/bankName/accountId");
        }
    }

    static private void removeAccount(String bankName, int accountId) {
        if (restClient.delete("removeAccount?bankName=" + bankName + "&accountId=" + accountId).equals("true")) {
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
