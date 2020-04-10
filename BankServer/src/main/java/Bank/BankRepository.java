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

    public boolean removeBank(Integer id) {
        if (banks.containsKey(id)) {
            banks.remove(id);
            return true;
        }
        return false;
    }

    public Bank getBank(Integer id) {
        if (banks.containsKey(id))
            return banks.get(id);
        return null;
    }
}
