package com.samuelryberg.expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class TransactionStorage {
  private final File file;
  private boolean headerChecked;

  private static final String HEADER = "Date;Amount;Note;Type;Category";

  TransactionStorage(String fileName) {
    this.file = new File(fileName);
  }

  public List<Transaction> readAll() throws IOException {
    List<Transaction> items = new ArrayList<>();

    if (!file.exists()) {
      return items;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String line;
      int lineNumber = 0;

      while ((line = br.readLine()) != null) {
        lineNumber++;

        if (lineNumber == 1) {
          if (!isHeaderValid(line)) {
            throw new IOException("Invalid header in transaction file: " + line);
          }
          headerChecked = true;
          continue;
        }

        if (line.trim().isEmpty()) {
          continue;
        }

        items.add(parseLine(line, lineNumber));
      }
    }

    return items;
  }

  public void append(Transaction t) throws IOException {
    ensureHeader();

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
      bw.write(format(t));
      bw.newLine();
    }
  }

  private boolean isHeaderValid(String line) {
    return line != null && line.startsWith(HEADER);
  }

  private synchronized void ensureHeader() throws IOException {
    if (headerChecked) {
      return;
    }

    if (!file.exists()) {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
        bw.write(HEADER);
        bw.newLine();
      }
      headerChecked = true;
      return;
    }

    if (file.length() == 0) {
      try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
        bw.write(HEADER);
        bw.newLine();
      }
      headerChecked = true;
      return;
    }

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
      String first = br.readLine();
      if (!isHeaderValid(first)) {
        throw new IOException("Cannot append to file. Header is not in a valid format.");
      }
    }

    headerChecked = true;
  }

  private String format(Transaction t) {
    return String.join(";",
        t.getDate().toString(),
        Float.toString(t.getAmount()),
        escape(t.getNote()),
        t.getType(),
        escape(t.getCategory()));
  }

  private String escape(String s) {
    if (s == null) {
      return "";
    }

    return s.replace(";", " ")
        .replace("\t", " ")
        .replace("\n", " ")
        .replace("\r", " ");
  }

  private Transaction parseLine(String line, int lineNumber) throws IOException {
    String[] parts = line.split(";", -1);

    if (parts.length < 5) {
      throw new IOException("Line " + lineNumber + " has too few columns: " + line);
    }

    try {
      LocalDate date = LocalDate.parse(parts[0]);
      float amount = Float.parseFloat(parts[1]);
      String note = parts[2];
      String type = parts[3] == null ? "" : parts[3].toLowerCase();
      String category = parts[4];

      if ("income".equals(type)) {
        return new Income(date, amount, note);
      } else if ("expense".equals(type) || type.isEmpty()) {
        return new Expense(date, amount, note, category);
      }

      throw new IOException("Unknown transaction type '" + parts[3] + "' on line " + lineNumber);
    } catch (RuntimeException e) {
      throw new IOException("Failed to parse line " + lineNumber + ": " + line, e);
    }
  }
}
