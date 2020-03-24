package com.bank.controller;


import com.bank.Bank;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(BankController.CONTRACT_BASE_URI)
public class BankController {

    public static final String CONTRACT_BASE_URI = "svc/v1/banks";

    @RequestMapping(value = "{bankNumber}")
    public Bank getBank(@PathVariable final int bankNumber) {
        Bank bank = new Bank();
        bank.setName("Milan");
        bank.setId(bankNumber);
        return bank;
    }
}
