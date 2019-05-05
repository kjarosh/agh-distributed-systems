package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountIdentification;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.InvalidAccount;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.InvalidCurrency;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.LoanAcknowledgement;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.LoanRequest;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.PremiumAccount;

/**
 * @author Kamil Jarosz
 */
public class PremiumAccountHandler extends StandardAccountHandler implements PremiumAccount.Iface {
    private static final Logger logger = LoggerFactory.getLogger(PremiumAccountHandler.class);

    private final AccountRepository accountRepository;
    private final ExchangeRateService exchangeRateService;

    public PremiumAccountHandler(AccountRepository accountRepository, ExchangeRateService exchangeRateService) {
        super(accountRepository);
        this.accountRepository = accountRepository;
        this.exchangeRateService = exchangeRateService;
    }

    @Override
    public LoanAcknowledgement takeLoan(
            AccountIdentification accountIdent, LoanRequest loanRequest) throws InvalidAccount, InvalidCurrency {
        logger.info("Received request for a loan: " + loanRequest);

        Account account = accountRepository.find(accountIdent);
        if (!account.isPremium()) {
            logger.info("Account is not premium");
            throw new InvalidAccount().setMessage("Account is not Premium");
        }

        String currency = loanRequest.currency;
        if (!BankServer.SUPPORTED_CURRENCIES.contains(currency)) {
            logger.info("Unsupported currency");
            throw new InvalidCurrency().setCurrency(currency).setMessage("Unsupported currency");
        }

        double exchangeRate = exchangeRateService.getExchangeRate(currency)
                .orElseThrow(() -> new InvalidCurrency().setCurrency(currency).setMessage("Currency not available"));

        long foreignPrice = calculateLoanPrice(loanRequest);
        long price = (long) (foreignPrice / exchangeRate);
        long loanValuePln = (long) (loanRequest.value / exchangeRate);

        account.addToBalance(loanValuePln);
        logger.info("The loan has been given: " + loanValuePln + ", cost: " + price + ", rate: " + exchangeRate);
        return new LoanAcknowledgement()
                .setPrice(price)
                .setForeignPrice(foreignPrice)
                .setExchangeRate(exchangeRate);
    }

    private long calculateLoanPrice(LoanRequest loanRequest) {
        return loanRequest.duration * loanRequest.value / 1000;
    }
}
