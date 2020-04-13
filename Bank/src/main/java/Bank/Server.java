package Bank;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class Server {
    private static int counter = 0;
    private final HashMap<String, Bank> banks = new HashMap<>();

    @PostMapping("/createBank")
    public int createBank(@RequestParam String name, @RequestParam String password) {
        banks.put(name, new Bank(name, password, counter));
        counter++;
        return counter-1;
    }

    @PostMapping("/createAccount")
    public int createAccount(@RequestParam String accountName,@RequestParam String bankName) {
        return banks.get(bankName).addAccount(accountName);
    }

    @GetMapping("/balance")
    public int balance(@RequestParam String bankName, @RequestParam int accountId) {
        return banks.get(bankName).getAccount(accountId).getBalance();
    }

    @PutMapping("/withdraw")
    public boolean withdraw(@RequestParam String bankName, @RequestParam int accountId, @RequestParam int amount) {
        return banks.get(bankName).getAccount(accountId).withdraw(amount);
    }

    @PutMapping("/deposit")
    public boolean deposit(@RequestParam String bankName, @RequestParam int accountId, @RequestParam int amount) {
        return banks.get(bankName).getAccount(accountId).deposit(amount);
    }

    @DeleteMapping("/removeAccount")
    public boolean removeAccount(@RequestParam String bankName, @RequestParam int accountId) {
        return  banks.get(bankName).removeAccount(accountId);
    }

    @DeleteMapping("/removeBank")
    public int removeBank(@RequestParam String bankName, @RequestParam String password) {
        if (banks.containsKey(bankName)) {
            if (banks.get(bankName).checkPassword(password)) {
                banks.remove(bankName);
                return 0; //everything ok
            } else
                return -2; //Wrong passwd
        } else
            return -1; //Doesn't exist
    }
}
