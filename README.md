# Java Expense Tracker

## Overview

This project is a lightweight command-line expense tracker written in Java. It lets you log income and expense transactions, stores them in a semicolon-delimited CSV file, and provides simple filtering and summary totals so you can review your cash flow.

## Key Features

- Interactive console workflow for adding either expenses or income entries.
- Input validation for amounts, dates, and transaction types to reduce bad data.
- Persistent storage in `transactions.csv`, created automatically on first run with a header row.
- Filtering by date range, category, and note keywords, with per-type totals and net balance calculation.

## Prerequisites

- Java 17 or newer (the project uses the Maven compiler).
- Maven 3.9+ (any recent Maven version should work).

## Build & Test

```bash
mvn clean package
```

The compiled application JAR will be created at `target/java-expense-tracker-1.0-SNAPSHOT.jar`.

## Running the CLI

```bash
java -cp target/java-expense-tracker-1.0-SNAPSHOT.jar \
  com.samuelryberg.expensetracker.Main
```

You will be prompted to choose between two modes:

- **Add** (`add` / `1`) — record a new transaction. The program asks for the type (expense or income), amount, note, optional category (for expenses, defaulting to `General`), and date (defaults to today if left blank). Successful entries are appended to `transactions.csv`.
- **Filter** (`filter` / `2`) — list existing transactions filtered by an optional date range, category, or note substring. Matching rows are printed with their type and category, followed by income, expense, and net totals.

After each operation, you can choose to run another command without restarting the application.

## Data File

- All transactions are saved to `transactions.csv` in the project root. The file uses the header `Date;Amount;Note;Type;Category`.
- If the file is missing it will be created automatically the first time you add a transaction.
- Notes and categories are sanitized to keep the CSV format consistent; semicolons and line breaks are replaced with spaces.
