package co.simplom.ATM;

import java.io.*;
import java.util.*;

class Account {
    private String accountNumber;
    private String pin;
    private double balance;

    public Account(String accountNumber, String pin, double balance) {
        this.accountNumber = accountNumber;
        this.pin = pin;
        this.balance = balance;
    }

    public boolean validatePin(String inputPin) {
        return this.pin.equals(inputPin);
    }

    public double getBalance() {
        return balance;
    }

    public boolean withdraw(double amount) {
        if (amount > 0 && amount % 10 == 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}

class CardReader {
    public boolean validatePin(Account account, String pin) {
        return account.validatePin(pin);
    }
}

class UserInterface {
    private Scanner scanner;

    public UserInterface() {
        this.scanner = new Scanner(System.in);
    }

    public String getInput(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    public void displayMessage(String message) {
        System.out.println(message);
    }
}

class Transaction {
    public static void logTransaction(String accountNumber, String type, double amount) {
        try (FileWriter writer = new FileWriter("transactions.log", true)) {
            writer.write(accountNumber + "," + type + "," + amount + "\n");
        } catch (IOException e) {
            System.out.println("Error logging transaction: " + e.getMessage());
        }
    }
}

class Bank {
    private static final String FILE_PATH = "accounts.txt";
    private Map<String, Account> accounts = new HashMap<>();

    public Bank() {
        loadAccounts();
    }

    private void loadAccounts() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                accounts.put(data[0], new Account(data[0], data[1], Double.parseDouble(data[2])));
            }
        } catch (IOException e) {
            System.out.println("Error loading accounts: " + e.getMessage());
        }
    }

    public Account getAccount(String accountNumber) {
        return accounts.get(accountNumber);
    }
}

class ATMController {
    private Bank bank;
    private CardReader cardReader;
    private UserInterface ui;

    public ATMController(Bank bank) {
        this.bank = bank;
        this.cardReader = new CardReader();
        this.ui = new UserInterface();
    }

    public void start() {
        String accountNumber = ui.getInput("Enter Account Number: ");
        Account account = bank.getAccount(accountNumber);
        
        if (account == null) {
            ui.displayMessage("Invalid account number.");
            return;
        }
        
        int attempts = 0;
        while (attempts < 3) {
            String pin = ui.getInput("Enter PIN: ");
            if (cardReader.validatePin(account, pin)) {
                break;
            } else {
                attempts++;
                ui.displayMessage("Invalid PIN. Attempts remaining: " + (3 - attempts));
            }
        }
        
        if (attempts == 3) {
            ui.displayMessage("Too many incorrect attempts. Exiting.");
            return;
        }
        
        while (true) {
            ui.displayMessage("\n1. Check Balance\n2. Withdraw\n3. Exit");
            int choice = Integer.parseInt(ui.getInput("Select option: "));
            
            switch (choice) {
                case 1:
                    ui.displayMessage("Your balance: $" + account.getBalance());
                    break;
                case 2:
                    double amount = Double.parseDouble(ui.getInput("Enter amount to withdraw (multiples of 10 only): "));
                    if (account.withdraw(amount)) {
                        Transaction.logTransaction(account.getAccountNumber(), "Withdraw", amount);
                        ui.displayMessage("Withdrawal successful. New balance: $" + account.getBalance());
                    } else {
                        ui.displayMessage("Invalid amount. Withdrawals must be in multiples of 10 and within your balance.");
                    }
                    break;
                case 3:
                    ui.displayMessage("Thank you for using the ATM.");
                    return;
                default:
                    ui.displayMessage("Invalid option. Try again.");
            }
        }
    }
}

public class ATM {
    public static void main(String[] args) {
        Bank bank = new Bank();
        ATMController atmController = new ATMController(bank);
        atmController.start();
    }
}

