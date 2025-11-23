package bank;

import java.util.ArrayList;
import java.util.List;

public class BankAccount {

    private String ownerName;
    private double balance;
    private List<Transaction> transactions;

    public BankAccount(String ownerName, double initialBalance) {
        this.ownerName = ownerName;
        this.balance = initialBalance;
        this.transactions = new ArrayList<>();
        transactions.add(new Transaction("Initial Balance", initialBalance));
    }

    public double getBalance() {
        return balance;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero");
        balance += amount;
        transactions.add(new Transaction("Deposit", amount));
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero");

        if (amount > balance) throw new IllegalArgumentException("Insufficient Funds");

        balance -= amount;
        transactions.add(new Transaction("Withdrawal", amount));
    }

    public void applyInterest(double ratePercent) {
        if (ratePercent < 0) throw new IllegalArgumentException("Rate cannot be negative");

        double interest = balance * (ratePercent / 100);
        balance += interest;
        transactions.add(new Transaction("Interest Added", interest));
    }
}
