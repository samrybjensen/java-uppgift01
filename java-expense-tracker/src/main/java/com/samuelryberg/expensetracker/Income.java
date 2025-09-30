package com.samuelryberg.expensetracker;

import java.time.LocalDate;

class Income extends Transaction {
  protected Income(LocalDate date, float amount, String note) {
    super(date, amount, note);
  }

  @Override
  public String getType() {
    return "income";
  }
}
