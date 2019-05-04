package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountCreationRequest;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountCreationResponse;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountManagement;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountType;

/**
 * @author Kamil Jarosz
 */
public class AccountManagementHandler implements AccountManagement.Iface {
    private static final Logger logger = LoggerFactory.getLogger(AccountManagementHandler.class);

    private final AccountRepository accountRepository;

    public AccountManagementHandler(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public AccountCreationResponse createAccount(AccountCreationRequest request) {
        logger.info("Creating new account: " + request);
        Account newAccount = new Account();
        newAccount.setFirstName(request.firstName);
        newAccount.setLastName(request.lastName);
        newAccount.setPesel(request.pesel);
        newAccount.setSalary(request.monthlySalary);
        accountRepository.addAccount(newAccount);

        return new AccountCreationResponse()
                .setKey(newAccount.getKey())
                .setType(newAccount.isPremium() ? AccountType.PREMIUM : AccountType.STANDARD);
    }
}
