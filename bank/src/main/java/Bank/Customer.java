package Bank;

public class Customer {
    String name;
    int id;
    int balance;

    public Customer(String name, int id) {
        this.name = name;
        this.id = id;
        this.balance = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBalance() {
        return balance;
    }

    public boolean withdraw(int amount) {
        if (amount < 0 || amount > balance) {
            return false;
        }
        else {
            balance -= amount;
            return true;
        }
    }

    public boolean deposit(int amount) {
        if (amount < 0) {
            return false;
        } else {
            balance += amount;
            return true;
        }
    }
}
