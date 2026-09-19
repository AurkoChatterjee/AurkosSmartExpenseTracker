# Smart Expense Tracker

A personal finance web app for logging daily expenses, categorizing them, and
viewing a spending summary — built to demonstrate CRUD operations, REST API
design, SQL aggregation, and full-stack integration.

## Stack

- **Backend:** Java 17, `com.sun.net.httpserver` (no framework), plain JDBC
- **Database:** MySQL
- **JSON:** Jackson
- **Logging:** SLF4J + Logback
- **Testing:** JUnit 5
- **Build:** Maven (shade plugin produces a runnable fat JAR)
- **Frontend:** Plain HTML/CSS/JavaScript, Chart.js (pie chart via CDN)

## Features

- Add, view, edit, and delete expenses (full CRUD)
- Categorize each expense (Food, Rent, Entertainment, Travel, Utilities, Health, Shopping, Other)
- Auto-computed summary: grand total, entry count, top category
- Spending breakdown by category rendered as a live-updating pie chart
- REST API with proper status codes (200/201/204/400/404/405/500) and input validation

## API Reference

| Method | Endpoint              | Description                          |
|--------|------------------------|---------------------------------------|
| GET    | `/api/expenses`        | List all expenses                     |
| POST   | `/api/expenses`        | Create an expense                     |
| GET    | `/api/expenses/{id}`   | Fetch a single expense                |
| PUT    | `/api/expenses/{id}`   | Update an expense                     |
| DELETE | `/api/expenses/{id}`   | Delete an expense                     |
| GET    | `/api/expenses/summary`| Category totals + grand total         |

Example request body (POST/PUT):

```json
{
  "title": "Groceries",
  "amount": 450.00,
  "category": "Food",
  "date": "2026-09-18",
  "notes": "Weekly shop"
}
```

## Setup

1. **Create the database user/password** you'll use, then edit
   `src/main/resources/application.properties`:
   ```
   db.url=jdbc:mysql://localhost:3306/expense_tracker?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   db.user=root
   db.password=your_mysql_password
   ```
   The database itself (`expense_tracker`) is auto-created on first run;
   the `expenses` table is created automatically via `schema.sql`.

2. **Build:**
   ```bash
   mvn clean package
   ```

3. **Run:**
   ```bash
   java -jar target/smart-expense-tracker.jar
   ```

4. Open **http://localhost:8080** in a browser. The dashboard is served
   directly by the same Java process (no separate frontend server needed).

## Running tests

```bash
mvn test
```

## Project structure

```
SmartExpenseTracker/
├── pom.xml
├── src/main/java/com/expensetracker/
│   ├── Main.java                  # HttpServer wiring
│   ├── model/Expense.java
│   ├── model/CategorySummary.java
│   ├── db/DatabaseManager.java    # connection + schema init
│   ├── db/ExpenseDao.java         # CRUD + aggregation SQL
│   ├── handler/ExpenseHandler.java    # REST API routing
│   ├── handler/StaticFileHandler.java # serves frontend/
│   └── util/JsonUtil.java
├── src/main/resources/
│   ├── application.properties
│   ├── schema.sql
│   └── logback.xml
├── src/test/java/com/expensetracker/ExpenseModelTest.java
└── frontend/
    ├── index.html
    ├── style.css
    └── app.js
```


