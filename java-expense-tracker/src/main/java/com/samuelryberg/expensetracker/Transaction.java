package com.samuelryberg.expensetracker;

import java.time.LocalDate;

abstract class Transaction {
  private LocalDate date;
  private float amount;
  private String note;

  protected Transaction(LocalDate date, float amount, String note) {
    this.date = date;
    this.amount = amount;
    this.note = note;
  }

  public LocalDate getDate() {
    return date;
  }

  public float getAmount() {
    return amount;
  }

  public String getNote() {
    return note;
  }
}
