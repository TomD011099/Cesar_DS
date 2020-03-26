package node;

/**
 * Client
 */
public class Node {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        RestNode node = new RestNode();
        System.out.println(node.post("create?name=Milan", null));
        System.out.println(node.get("balance?id=1")); // get balance
    }
}
