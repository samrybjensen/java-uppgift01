import java.time.LocalDate;
import java.util.Scanner;

public class Main {
  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);

    System.out.print("Enter a price: ");
    float amount = scanner.nextFloat();

    System.out.print("Enter a note: ");
    String note = scanner.next();

    Expense expense = new Expense(LocalDate.now(), amount, note);

    System.out.println(expense.getDate() + " " + expense.getNote() + " " + expense.getAmount());

    scanner.close();
  }
}
