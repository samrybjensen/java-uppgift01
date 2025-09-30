package com.samuelryberg.expensetracker;

import java.time.LocalDate;

class Expense extends Transaction {
  private String category;

  protected Expense(LocalDate date, float amount, String note, String category) {
    super(date, amount, note);
    this.category = category;
  }

  public String getCategory() {
    return category;
  }

  @Override
  public String getType() {
    return "expense";
  }
}
