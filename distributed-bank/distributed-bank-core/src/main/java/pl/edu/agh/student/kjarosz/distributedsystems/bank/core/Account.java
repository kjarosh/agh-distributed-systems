package pl.edu.agh.student.kjarosz.distributedsystems.bank.core;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Kamil Jarosz
 */
public class Account {
    private static final int PREMIUM_MINIMAL_SALARY = 500000;
    private final String key = UUID.randomUUID().toString();
    private String pesel;
    private String lastName;
    private String firstName;
    private long salary;
    private AtomicLong balance = new AtomicLong(0);

    public String getKey() {
        return key;
    }

    public long getBalance() {
        return balance.get();
    }

    public void addToBalance(long difference) {
        balance.addAndGet(difference);
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setSalary(long monthlySalary) {
        this.salary = monthlySalary;
    }

    public boolean isPremium() {
        return this.salary >= PREMIUM_MINIMAL_SALARY;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }
}
