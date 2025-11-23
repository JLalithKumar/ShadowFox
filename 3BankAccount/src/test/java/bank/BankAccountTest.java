package bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BankAccountTest {

    private BankAccount bank;

    @BeforeEach
    void setup() {
        bank = new BankAccount("Lalith", 1000);
    }

    // --------------------- Deposit Tests ---------------------

    @Test
    void testDepositIncreasesBalance() {
        bank.deposit(500);
        assertEquals(1500, bank.getBalance());
    }

    @Test
    void testDepositZeroThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> bank.deposit(0));
    }

    @Test
    void testDepositNegativeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> bank.deposit(-100));
    }

    @Test
    void testDepositTransactionAdded() {
        bank.deposit(300);
        assertEquals(2, bank.getTransactions().size());
    }

    @Test
    void testDepositAmountCorrect() {
        bank.deposit(300);
        assertEquals(300, bank.getTransactions().get(1).getAmount());
    }

    // --------------------- Withdrawal Tests ---------------------

    @Test
    void testWithdrawReducesBalance() {
        bank.withdraw(200);
        assertEquals(800, bank.getBalance());
    }

    @Test
    void testWithdrawMoreThanBalanceThrows() {
        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(2000));
    }

    @Test
    void testWithdrawNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(-1));
    }

    @Test
    void testWithdrawZeroThrows() {
        assertThrows(IllegalArgumentException.class, () -> bank.withdraw(0));
    }

    @Test
    void testWithdrawTransactionAdded() {
        bank.withdraw(150);
        assertEquals(2, bank.getTransactions().size());
    }

    // --------------------- Balance Inquiry Tests ---------------------

    @Test
    void testInitialBalanceCorrect() {
        assertEquals(1000, bank.getBalance());
    }

    @Test
    void testBalanceAfterMultipleOperations() {
        bank.deposit(200);
        bank.withdraw(100);
        bank.deposit(50);
        assertEquals(1150, bank.getBalance());
    }

    // --------------------- Interest Tests ---------------------

    @Test
    void testInterestAddedCorrectly() {
        bank.applyInterest(10);
        assertEquals(1100, bank.getBalance());
    }

    @Test
    void testInterestNegativeThrows() {
        assertThrows(IllegalArgumentException.class, () -> bank.applyInterest(-5));
    }

    @Test
    void testInterestTransactionAdded() {
        bank.applyInterest(5);
        assertEquals(2, bank.getTransactions().size());
    }
}
