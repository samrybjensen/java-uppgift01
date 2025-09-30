package com.samuelryberg.expensetracker;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Program {
  private static final String STORAGE_FILE = "transactions.csv";
  private final Scanner scanner;

  public Program() {
    this.scanner = new Scanner(System.in);
  }

  public void run() {
    System.out.println("Choose a mode: 'add' or 'filter'");
    System.out.print("Enter choice (add/filter or 1/2): ");
    String choice = scanner.nextLine().trim().toLowerCase();

    if ("1".equals(choice)) {
      choice = "add";
    }
    if ("2".equals(choice)) {
      choice = "filter";
    }

    switch (choice) {
      case "add":
        interactiveAdd();
        break;
      case "filter":
        interactiveFilter();
        break;
      default:
        System.out.println("Unknown choice. Please type 'add' or 'filter'.");
    }
  }

  public boolean promptAgain() {
    System.out.print("Perform another request? (Y/n): ");

    String answer = scanner.nextLine().trim();
    if (answer.isEmpty()) {
      return true;
    }

    char c = Character.toLowerCase(answer.charAt(0));
    return c == 'y';
  }

  private void interactiveAdd() {
    System.out.println("-- Add Transaction --");

    String type = null;
    for (int attempts = 0; attempts < 3 && type == null; attempts++) {
      System.out.print("Enter choice (expense/income or 1/2): ");
      String input = scanner.nextLine().trim().toLowerCase();

      if (input.equals("1")) {
        input = "expense";
      }

      if (input.equals("2")) {
        input = "income";
      }

      if ("expense".equals(input) || "income".equals(input)) {
        type = input;
        break;
      } else {
        System.out.println("Please type 'expense' or 'income'.");
      }
    }

    if (type == null) {
      System.out.println("No valid type selected. Cancelling add.");
      return;
    }

    Float amount = null;
    for (int attempts = 0; attempts < 3 && amount == null; attempts++) {
      System.out.print("Amount: ");
      String line = scanner.nextLine().trim();

      try {
        amount = Float.parseFloat(line);
      } catch (NumberFormatException e) {
        System.out.println("Invalid number. Try again (" + (2 - attempts) + " left).");
      }
    }

    if (amount == null) {
      System.out.println("Could not parse amount. Cancelling add.");
      return;
    }

    System.out.print("Note: ");
    String note = scanner.nextLine().trim();

    String category = null;

    if ("expense".equals(type)) {
      System.out.print("Category (default 'General'): ");
      category = scanner.nextLine().trim();

      if (category.isEmpty()) {
        category = "General";
      }
    }

    System.out.print("Date YYYY-MM-DD (blank for today): ");
    String dateStr = scanner.nextLine().trim();
    LocalDate date = LocalDate.now();

    if (!dateStr.isEmpty()) {
      try {
        date = LocalDate.parse(dateStr);
      } catch (DateTimeParseException e) {
        System.out.println("Invalid date, using today.");
      }
    }

    Transaction tx;

    if ("income".equals(type)) {
      tx = new Income(date, amount, note);
    } else {
      tx = new Expense(date, amount, note, category);
    }

    TransactionStorage store = new TransactionStorage(STORAGE_FILE);

    try {
      store.append(tx);

      System.out.println("Saved: " + formatTransaction(tx));
    } catch (Exception e) {
      System.out.println("Failed to save: " + e.getMessage());
    }
  }

  private void interactiveFilter() {
    System.out.println("-- Filter Transactions --");

    System.out.print("From date YYYY-MM-DD (blank = no min): ");
    String fromStr = scanner.nextLine().trim();
    System.out.print("To date YYYY-MM-DD (blank = no max): ");
    String toStr = scanner.nextLine().trim();
    System.out.print("Category (blank = any): ");
    String category = scanner.nextLine().trim();

    if (category.isEmpty()) {
      category = null;
    }

    System.out.print("Note contains (blank = any): ");
    String noteContains = scanner.nextLine().trim();

    if (noteContains.isEmpty()) {
      noteContains = null;
    }

    LocalDate from = null;
    LocalDate to = null;

    try {
      if (!fromStr.isEmpty()) {
        from = LocalDate.parse(fromStr);
      }

      if (!toStr.isEmpty()) {
        to = LocalDate.parse(toStr);
      }
    } catch (DateTimeParseException e) {
      System.out.println("Invalid date. Use YYYY-MM-DD.");
      return;
    }

    TransactionStorage store = new TransactionStorage(STORAGE_FILE);
    List<Transaction> all;

    try {
      all = store.readAll();
    } catch (Exception e) {
      System.out.println("Failed to read: " + e.getMessage());
      return;
    }

    List<Transaction> filtered = new ArrayList<>();

    for (Transaction t : all) {
      if (from != null && t.getDate().isBefore(from)) {
        continue;
      }

      if (to != null && t.getDate().isAfter(to)) {
        continue;
      }

      if (category != null) {
        String transactionCategory = t.getCategory();
        if (transactionCategory.isEmpty() || !category.equalsIgnoreCase(transactionCategory)) {
          continue;
        }
      }

      if (noteContains != null && !t.getNote().toLowerCase().contains(noteContains.toLowerCase())) {
        continue;
      }

      filtered.add(t);
    }

    if (filtered.isEmpty()) {
      System.out.println("No matching transactions.");
      return;
    }

    System.out.println("Date Amount Note Type Category");

    float incomeSum = 0f;
    float expenseSum = 0f;

    for (Transaction t : filtered) {
      String formatted = formatTransaction(t);
      System.out.println(formatted);

      if (t.isExpense()) {
        expenseSum += t.getAmount();
      } else {
        incomeSum += t.getAmount();
      }
    }

    float net = incomeSum - expenseSum;

    System.out.println("-- income total: " + incomeSum);
    System.out.println("-- expense total: " + expenseSum);
    System.out.println("-- net total: " + net);
  }

  private static String formatTransaction(Transaction t) {
    return String.join(";",
        t.getDate().toString(),
        Float.toString(t.getAmount()),
        escapeForDisplay(t.getNote()),
        t.getType(),
        escapeForDisplay(t.getCategory()));
  }

  private static String escapeForDisplay(String value) {
    if (value == null) {
      return "";
    }

    return value.replace(";", " ")
        .replace("\t", " ")
        .replace("\n", " ")
        .replace("\r", " ");
  }
}
