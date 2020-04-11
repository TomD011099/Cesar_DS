package Bank.Controllers;

public class Bank {
    private String name;
    private int id;
    private int balance;

    Bank(String name, int id) {
        this.name = name;
        this.id = id;
        this.balance = 0;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int withdraw(int amount) {
        if (!((balance - amount) < 0))
            balance -= amount;
        return balance;
    }

    public int deposit(int amount) {
        balance += amount;
        return balance;
    }
}
