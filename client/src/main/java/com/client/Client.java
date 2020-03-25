package com.client;

/**
 * Client
 */
public class Client {

    public static void main(String[] args) {
        RestClient client = new RestClient();
        System.out.println(client.get("customer"));
    }
}