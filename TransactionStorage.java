import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

class TransactionStorage {
  private final Path filePath;

  TransactionStorage(String fileName) {
    this.filePath = Paths.get(fileName);
  }

  public List<Expense> readAll() throws IOException {
    List<Expense> items = new ArrayList<>();
    if (!Files.exists(filePath)) {
      return items;
    }
    try (BufferedReader br = new BufferedReader(new FileReader(filePath.toFile()))) {
      String line;
      br.mark(1024);
      String first = br.readLine();
      if (first != null) {
        if (!isHeader(first)) {
          Expense e = parseLine(first);
          if (e != null)
            items.add(e);
        }
      }
      if (first != null) {
        while ((line = br.readLine()) != null) {
          Expense e = parseLine(line);
          if (e != null)
            items.add(e);
        }
      }
    }
    return items;
  }

  public void append(Expense expense) throws IOException {
    File f = filePath.toFile();
    boolean exists = f.exists();
    try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
      if (!exists) {
        bw.write("date\tamount\tnote\tcategory\n");
      }
      bw.write(format(expense));
      bw.newLine();
    }
  }

  private boolean isHeader(String line) {
    return line != null && line.startsWith("date\tamount\tnote\tcategory");
  }

  private String format(Expense e) {
    return String.join("\t",
        e.getDate().toString(),
        Float.toString(e.getAmount()),
        escape(e.getNote()),
        escape(e.getCategory()));
  }

  private String escape(String s) {
    if (s == null)
      return "";
    return s.replace("\t", " ").replace("\n", " ").replace("\r", " ");
  }

  private Expense parseLine(String line) {
    if (line == null || line.isEmpty())
      return null;
    String[] parts = line.split("\t", -1);
    if (parts.length < 4)
      return null;
    try {
      LocalDate date = LocalDate.parse(parts[0]);
      float amount = Float.parseFloat(parts[1]);
      String note = parts[2];
      String category = parts[3];
      return new Expense(date, amount, note, category);
    } catch (Exception e) {
      return null;
    }
  }
}
