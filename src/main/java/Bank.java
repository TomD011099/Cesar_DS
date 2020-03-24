package com.bank;


public class Bank {

    private String name;
    private int id;

    public Bank() {
        name = "N/A";
        id = -1;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }
}
