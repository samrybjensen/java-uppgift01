import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
  private static final String STORAGE_FILE = "transactions.tsv";

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.println("Choose a mode: 'add' or 'filter'");
    System.out.print("Enter choice (add/filter or 1/2): ");
    String choice = scanner.nextLine().trim().toLowerCase();
    if ("1".equals(choice))
      choice = "add";
    if ("2".equals(choice))
      choice = "filter";

    switch (choice) {
      case "add":
        interactiveAdd(scanner);
        break;
      case "filter":
        interactiveFilter(scanner);
        break;
      default:
        System.out.println("Unknown choice. Please type 'add' or 'filter'.");
    }

    scanner.close();
  }

  private static void interactiveAdd(Scanner scanner) {
    System.out.println("-- Add Expense --");

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

    System.out.print("Category (default 'General'): ");
    String category = scanner.nextLine().trim();
    if (category.isEmpty())
      category = "General";

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

    Expense expense = new Expense(date, amount, note, category);
    TransactionStorage store = new TransactionStorage(STORAGE_FILE);
    try {
      store.append(expense);
      System.out.println("Saved: " + date + "\t" + amount + "\t" + note + "\t" + category);
    } catch (Exception e) {
      System.out.println("Failed to save: " + e.getMessage());
    }
  }

  private static void interactiveFilter(Scanner scanner) {
    System.out.println("-- Filter Expenses --");

    System.out.print("From date YYYY-MM-DD (blank = no min): ");
    String fromStr = scanner.nextLine().trim();
    System.out.print("To date YYYY-MM-DD (blank = no max): ");
    String toStr = scanner.nextLine().trim();
    System.out.print("Category (blank = any): ");
    String category = scanner.nextLine().trim();
    if (category.isEmpty())
      category = null;
    System.out.print("Note contains (blank = any): ");
    String noteContains = scanner.nextLine().trim();
    if (noteContains.isEmpty())
      noteContains = null;

    LocalDate from = null;
    LocalDate to = null;
    try {
      if (!fromStr.isEmpty())
        from = LocalDate.parse(fromStr);
      if (!toStr.isEmpty())
        to = LocalDate.parse(toStr);
    } catch (DateTimeParseException e) {
      System.out.println("Invalid date. Use YYYY-MM-DD.");
      return;
    }

    TransactionStorage store = new TransactionStorage(STORAGE_FILE);
    List<Expense> all;
    try {
      all = store.readAll();
    } catch (Exception e) {
      System.out.println("Failed to read: " + e.getMessage());
      return;
    }

    List<Expense> filtered = new ArrayList<>();
    for (Expense e : all) {
      if (from != null && e.getDate().isBefore(from))
        continue;
      if (to != null && e.getDate().isAfter(to))
        continue;
      if (category != null && !category.equalsIgnoreCase(e.getCategory()))
        continue;
      if (noteContains != null && !e.getNote().toLowerCase().contains(noteContains.toLowerCase()))
        continue;
      filtered.add(e);
    }

    if (filtered.isEmpty()) {
      System.out.println("No matching transactions.");
      return;
    }

    System.out.println("date\tamount\tnote\tcategory");
    float sum = 0f;
    for (Expense e : filtered) {
      System.out.println(e.getDate() + "\t" + e.getAmount() + "\t" + e.getNote() + "\t" + e.getCategory());
      sum += e.getAmount();
    }
    System.out.println("-- total: " + sum);
  }
}
