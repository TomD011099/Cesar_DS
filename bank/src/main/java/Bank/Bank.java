package Bank;

import java.util.HashMap;

public class Bank {
    private String name;
    private int bankId;
    private int accountCounter;
    private HashMap<Integer, Customer> accounts;

    Bank(String name, int bankId) {
        this.name = name;
        this.bankId = bankId;
        this.accounts = new HashMap<>();
        this.accountCounter = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getBankId() {
        return bankId;
    }

    public void setBankId(int bankId) {
        this.bankId = bankId;
    }

    public int addCustomer(String name) {
        accounts.put(accountCounter, new Customer(name, accountCounter));
        accountCounter++;
        return accountCounter-1;
    }

    public Customer getCustomer(int id) {
        return accounts.getOrDefault(id, null);
    }

    public boolean removeCustomer(int id) {
        if (accounts.containsKey(id)) {
            accounts.remove(id);
            return true;
        } else {
            return false;
        }
    }
}
