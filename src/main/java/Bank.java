import java.util.HashMap;
import java.util.Map;

public class Bank {
    private Map<String, Integer> balanceMap = new HashMap<>();

    //Singleton code
    private static Bank instance;

    public static Bank getInstance() {
        if (instance == null) {
            instance = new Bank();
        }
        return instance;
    }

    private Bank() {
        balanceMap.put("Master", 0);
    }

    public Integer getBalance(String name) {
        return balanceMap.get(name);
    }

    public void updatePerson(String name, Integer balance) {
        balanceMap.put(name, balance);
    }
}
