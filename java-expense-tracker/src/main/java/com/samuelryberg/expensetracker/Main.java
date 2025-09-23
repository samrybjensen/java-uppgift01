package com.samuelryberg.expensetracker;

public class Main {
  public static void main(String[] args) {
    Program program = new Program();
    do {
      program.Run();
    } while (program.PromptAgain());
  }
}
