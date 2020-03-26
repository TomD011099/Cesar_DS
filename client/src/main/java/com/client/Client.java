package com.client;

/**
 * Client
 */
public class Client {

    public static void main(String[] args) {
        RestClient client = new RestClient();
        System.out.println(client.post("create?name=Milan", null));
        System.out.println(client.get("balance?id=1")); // get balance
        System.out.println(client.get("customers"));
    }
}