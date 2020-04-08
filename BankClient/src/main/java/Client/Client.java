package Client;

/**
 * Client
 */
public class Client {

    public static void main(String[] args) {
        RestClient node = new RestClient("localhost");
        System.out.println("Create account:");
        System.out.println(node.post("create?name=Milan", null));
        System.out.println("Deposit €500: " + node.put("deposit?id=1&amount=500", null));
        System.out.println(node.get("balance?id=1"));
    }
}
