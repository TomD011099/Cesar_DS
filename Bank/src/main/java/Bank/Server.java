package Bank;

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
public class Server {
    private static int counter = 0;
    private final HashMap<String, Bank> banks = new HashMap<>();

    @PostMapping("/createBank")
    public int createBank(@RequestParam String name) {
        if (!banks.containsKey(name)) {
            banks.put(name, new Bank(name, counter));
            counter++;
            return counter - 1;
        } else
            return -1;
    }

    @PostMapping("/createAccount")
    public int createAccount(@RequestParam String accountName, @RequestParam String bankName) {
        return banks.get(bankName).addAccount(accountName);
    }

    @GetMapping("/balance")
    public int balance(@RequestParam String bankName, @RequestParam int accountId) {
        if (banks.containsKey(bankName)) {
            Account temp = banks.get(bankName).getAccount(accountId);
            if (temp != null) {
                return temp.getBalance();
            }
        }
        return -1;
    }

    @PutMapping("/withdraw")
    public boolean withdraw(@RequestParam String bankName, @RequestParam int accountId, @RequestParam int amount) {
        if (banks.containsKey(bankName)) {
            Account temp = banks.get(bankName).getAccount(accountId);
            if (temp != null) {
                return temp.withdraw(amount);
            }
        }
        return false;
    }

    @PutMapping("/deposit")
    public boolean deposit(@RequestParam String bankName, @RequestParam int accountId, @RequestParam int amount) {
        if (banks.containsKey(bankName)) {
            Account temp = banks.get(bankName).getAccount(accountId);
            if (temp != null) {
                return temp.deposit(amount);
            }
        }
        return false;
    }

    @DeleteMapping("/removeAccount")
    public boolean removeAccount(@RequestParam String bankName, @RequestParam int accountId) {
        if (banks.containsKey(bankName)) {
            return banks.get(bankName).removeAccount(accountId);
        }
        return false;
    }

    @DeleteMapping("/removeBank")
    public boolean removeBank(@RequestParam String bankName) {
        if (banks.containsKey(bankName)) {
            banks.remove(bankName);
            return true; //everything ok
        } else
            return false; //Doesn't exist
    }
}
