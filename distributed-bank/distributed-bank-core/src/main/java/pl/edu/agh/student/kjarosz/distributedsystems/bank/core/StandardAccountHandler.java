package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountIdentification;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.InvalidAccount;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.StandardAccount;

/**
 * @author Kamil Jarosz
 */
public class StandardAccountHandler implements StandardAccount.Iface {
    private static final Logger logger = LoggerFactory.getLogger(StandardAccountHandler.class);

    private final AccountRepository accountRepository;

    public StandardAccountHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public long accountBalance(AccountIdentification accountIdent) throws InvalidAccount {
        logger.info("Asking for account balance: " + accountIdent);

        Account account = accountRepository.find(accountIdent.pesel, accountIdent.key);
        return account.getBalance();
    }
}
