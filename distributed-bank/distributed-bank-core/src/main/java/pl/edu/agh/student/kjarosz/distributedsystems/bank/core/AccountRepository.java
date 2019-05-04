package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.InvalidAccount;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kamil Jarosz
 */
public class AccountRepository {
    private Map<String, Account> accounts = new ConcurrentHashMap<>();

    public Account find(String pesel, String key) throws InvalidAccount {
        Account account = accounts.get(pesel);
        if (account == null) {
            throw new InvalidAccount().setMessage("Account doesn't exist");
        }

        if (!account.getKey().equals(key)) {
            throw new InvalidAccount().setMessage("Wrong key");
        }

        return account;
    }

    public void addAccount(Account acc) {
        accounts.put(acc.getPesel(), acc);
    }
}
