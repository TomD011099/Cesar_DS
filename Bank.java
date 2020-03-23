public class Bank {

    private int balance;

    public Bank() {
        balance = 0;
    }

    public int getBalance() {
        return balance;
    }

    public void deposit(int amount) {
        balance += amount;
    }

    public int withdraw(int amount) {
        if (((balance-amount) < 0) || amount <= 0)
            return -1;
        else {
            balance -= amount;
            return balance;
        }
    }
}
