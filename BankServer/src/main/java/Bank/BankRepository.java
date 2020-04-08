package Bank;


import java.util.HashMap;

public class BankRepository {

    private HashMap<Integer, Bank> banks;

    BankRepository() {
        banks = new HashMap<>();
    }

    public void addBank(Bank bank) {
        banks.put(bank.getId(), bank);
    }

    public Bank removeBank(Integer id) {
        return banks.remove(id);
    }

    public Bank getBank(Integer id) {
        return banks.get(id);
    }
}
