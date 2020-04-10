package Bank;


import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.web.bind.annotation.*;

@RestController
public class BankController {

    private static final AtomicInteger counter = new AtomicInteger();
    private final BankRepository bankRepository = new BankRepository();

    // POST: create a new bank account
    @PostMapping("/create")
    public int createBank(@RequestParam String name) {
        Bank bank = new Bank(name, counter.incrementAndGet());
        bankRepository.addBank(bank);
        return bank.getId();
    }

    // GET: show the current balance
    @GetMapping("/balance")
    public int getBalance(@RequestParam int id) {
        return bankRepository.getBank(id).getBalance();
    }

    // PUT: place a deposit
    @PutMapping("/deposit")
    public boolean deposit(@RequestParam int id, @RequestParam int amount) {
        return bankRepository.getBank(id).deposit(amount);
    }

    // PUT: place a withdraw
    @PutMapping("/withdraw")
    public boolean withdraw(@RequestParam int id, @RequestParam int amount) {
        return bankRepository.getBank(id).withdraw(amount);
    }

    // DELETE: delete a bank account
    @DeleteMapping("/delete")
    public boolean delete(@RequestParam int id) {
        return bankRepository.removeBank(id);
    }
}
