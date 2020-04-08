package Bank;


import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.*;

@RestController
public class BankController {

    private static final AtomicInteger counter = new AtomicInteger();
    private final BankRepository bankRepository = new BankRepository();

    // POST: create a new bank account
    @PostMapping("/create")
    public Bank createBank(@RequestParam String name) {
        Bank bank = new Bank(name, counter.incrementAndGet());
        bankRepository.addBank(bank);
        return bank;
    }

    // GET: show the current balance
    @GetMapping("/balance")
    public String getBalance(@RequestParam int id) {
        return "Balance: " + bankRepository.getBank(id).getBalance();
    }

    // PUT: place a deposit
    @PutMapping("/deposit")
    public String deposit(@RequestParam int id, @RequestParam int amount) {
        return "Balance: " + bankRepository.getBank(id).deposit(amount);
    }

    // PUT: place a withdraw
    @PutMapping("/withdraw")
    public String withdraw(@RequestParam int id, @RequestParam int amount) {
        return "Balance: " + bankRepository.getBank(id).withdraw(amount);
    }

    // DELETE: delete a bank account
    @DeleteMapping("/delete")
    public String delete(@RequestParam int id) {
        return "Deleted: \n" + bankRepository.removeBank(id);
    }
}
