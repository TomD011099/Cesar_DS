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
    public void deposit(@RequestParam int id, @RequestParam int amount) {
        Thread depositThread = new DepositThread(bankRepository.getBank(id), amount);
        depositThread.start();
    }

    // PUT: place a withdraw
    @PutMapping("/withdraw")
    public void withdraw(@RequestParam int id, @RequestParam int amount) {
        Thread withdrawThread = new WithdrawThread(bankRepository.getBank(id), amount);
        withdrawThread.start();
    }

    // DELETE: delete a bank account
    @DeleteMapping("/delete")
    public boolean delete(@RequestParam int id) {
        return bankRepository.removeBank(id);
    }
}
