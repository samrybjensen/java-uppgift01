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

  private static final String HEADER = "date\tamount\tnote\ttype\tcategory";

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
      String first = br.readLine();

      if (first != null) {
        if (!isHeaderValid(first)) {
          Transaction t = parseLine(first);

          if (t != null) {
            items.add(t);
          }
        }
      }

      if (first != null) {
        while ((line = br.readLine()) != null) {
          Transaction t = parseLine(line);
          if (t != null)
            items.add(t);
        }
      }
    }

    return items;
  }

  public void append(Transaction t) throws IOException {
    File f = this.file;
    boolean exists = f.exists();

    if (exists && f.length() > 0) {
      try (BufferedReader br = new BufferedReader(new FileReader(f))) {
        String first = br.readLine();
        if (first != null && !isHeaderValid(first)) {
          throw new IOException("Cannot append to file. Header is not in a valid format.");
        }
      }
    }

    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
      if (!exists) {
        bw.write(HEADER + "\n");
      } else if (f.length() == 0) {
        bw.write(HEADER + "\n");
      }

      bw.write(format(t));
      bw.newLine();
    }
  }

  private boolean isHeaderValid(String line) {
    return line != null && line.startsWith(HEADER);
  }

  private String format(Transaction t) {
    String type;
    String category = "";

    if (t instanceof Expense) {
      type = "expense";
      category = ((Expense) t).getCategory();
    } else if (t instanceof Income) {
      type = "income";
    } else {
      type = "unknown";
    }

    return String.join("\t",
        t.getDate().toString(),
        Float.toString(t.getAmount()),
        escape(t.getNote()),
        type,
        escape(category));
  }

  private String escape(String s) {
    if (s == null) {
      return "";
    }

    return s.replace("\t", " ").replace("\n", " ").replace("\r", " ");
  }

  private Transaction parseLine(String line) {
    if (line == null || line.isEmpty()) {
      return null;
    }

    String[] parts = line.split("\t", -1);

    try {
      if (parts.length < 5) {
        return null;
      }

      LocalDate date = LocalDate.parse(parts[0]);
      float amount = Float.parseFloat(parts[1]);
      String note = parts[2];
      String type = parts[3] == null ? "" : parts[3].toLowerCase();
      String category = parts[4];

      if ("income".equals(type)) {
        return new Income(date, amount, note);
      } else if ("expense".equals(type) || type.isEmpty()) {
        return new Expense(date, amount, note, category);
      } else {
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }
}
