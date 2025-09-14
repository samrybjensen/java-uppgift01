import java.time.LocalDate;

class Expense extends Transaction {
  protected Expense(LocalDate date, float amount, String note) {
    super(date, amount, note);
  }
}
