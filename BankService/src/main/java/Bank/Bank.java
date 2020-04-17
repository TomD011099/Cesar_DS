package Bank;

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

    public synchronized void withdraw(int amount) {
        if (amount < 0 || amount > this.balance)
            return;
        balance -= amount;
    }

    public synchronized void deposit(int amount) {
        if (amount < 0)
            return;
        balance += amount;
    }
}
