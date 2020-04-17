package Bank;

public class WithdrawThread extends Thread {

    private Bank bank;
    private int amount;

    WithdrawThread(Bank bank, int amount) {
        this.bank = bank;
        this.amount =  amount;
    }

    @Override
    public void run() {
        bank.withdraw(amount);
    }
}
