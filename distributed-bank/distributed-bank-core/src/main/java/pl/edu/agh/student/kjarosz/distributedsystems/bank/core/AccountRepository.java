package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.AccountIdentification;
import pl.edu.agh.student.kjarosz.distributedsystems.bank.api.InvalidAccount;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Kamil Jarosz
 */
public class AccountRepository {
    private static final MessageDigest md5;

    static {
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
    }

    private Map<String, Account> accounts = new ConcurrentHashMap<>();

    public void addAccount(Account acc) {
        accounts.put(acc.getPesel(), acc);
    }

    public Account find(AccountIdentification accountIdent) throws InvalidAccount {
        Account account = accounts.get(accountIdent.pesel);
        if (account == null) {
            throw new InvalidAccount().setMessage("Account doesn't exist");
        }

        verifySignature(account, accountIdent);

        return account;
    }

    private void verifySignature(Account account, AccountIdentification accountIdent) throws InvalidAccount {
        long seqid = accountIdent.getSeqid();
        long expectedSeqid = account.nextSeqid();
        if (seqid != expectedSeqid) {
            throw new InvalidAccount().setMessage("Invalid seqid: " + seqid).setCurrentSeqid(expectedSeqid);
        }

        String document = "" + account.getPesel() + ":" + seqid + ":" + account.getKey();
        byte[] expectedSignature = md5.digest(document.getBytes());

        if (!Arrays.equals(accountIdent.getSignature(), expectedSignature)) {
            throw new InvalidAccount().setMessage("Invalid signature").setCurrentSeqid(expectedSeqid);
        }
    }
}
