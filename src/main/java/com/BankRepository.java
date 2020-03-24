package com;

import java.util.ArrayList;

public class BankRepository {

    private ArrayList<Bank> banks = new ArrayList<>();

    BankRepository() {}

    public void addBank(Bank bank) {
        banks.add(bank);
    }

    public Bank removeBank(int id) {
        return banks.remove(id);
    }

    public Bank getBank(int id) {
        return banks.get(id);
    }
}
