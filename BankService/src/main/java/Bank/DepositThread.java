package Bank;

public class DepositThread extends Thread {

    private Bank bank;
    private int amount;

    DepositThread(Bank bank, int amount) {
        this.bank = bank;
        this.amount = amount;
    }

    @Override
    public void run() {
        bank.deposit(amount);
    }
}
