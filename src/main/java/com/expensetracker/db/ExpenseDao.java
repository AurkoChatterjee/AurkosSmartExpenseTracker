package com.expensetracker.db;

import com.expensetracker.model.CategorySummary;
import com.expensetracker.model.Expense;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Plain JDBC data access layer for expenses. No ORM — every query is
 * explicit so it's easy to explain and defend in a viva.
 */
public class ExpenseDao {

    private static final Logger log = LoggerFactory.getLogger(ExpenseDao.class);

    private final DatabaseManager db;

    public ExpenseDao(DatabaseManager db) {
        this.db = db;
    }

    public Expense create(Expense e) {
        String sql = "INSERT INTO expenses (title, amount, category, expense_date, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getTitle());
            ps.setDouble(2, e.getAmount());
            ps.setString(3, e.getCategory());
            ps.setDate(4, Date.valueOf(e.getDate()));
            ps.setString(5, e.getNotes());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    e.setId(keys.getLong(1));
                }
            }
            return e;
        } catch (SQLException ex) {
            log.error("Failed to insert expense", ex);
            throw new RuntimeException("Failed to insert expense", ex);
        }
    }

    public List<Expense> findAll() {
        String sql = "SELECT * FROM expenses ORDER BY expense_date DESC, id DESC";
        List<Expense> results = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(mapRow(rs));
            }
            return results;
        } catch (SQLException ex) {
            log.error("Failed to fetch expenses", ex);
            throw new RuntimeException("Failed to fetch expenses", ex);
        }
    }

    public Expense findById(long id) {
        String sql = "SELECT * FROM expenses WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            log.error("Failed to fetch expense {}", id, ex);
            throw new RuntimeException("Failed to fetch expense", ex);
        }
    }

    /** Returns true if a row was updated. */
    public boolean update(long id, Expense e) {
        String sql = "UPDATE expenses SET title = ?, amount = ?, category = ?, expense_date = ?, notes = ? WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, e.getTitle());
            ps.setDouble(2, e.getAmount());
            ps.setString(3, e.getCategory());
            ps.setDate(4, Date.valueOf(e.getDate()));
            ps.setString(5, e.getNotes());
            ps.setLong(6, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            log.error("Failed to update expense {}", id, ex);
            throw new RuntimeException("Failed to update expense", ex);
        }
    }

    /** Returns true if a row was deleted. */
    public boolean delete(long id) {
        String sql = "DELETE FROM expenses WHERE id = ?";
        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            log.error("Failed to delete expense {}", id, ex);
            throw new RuntimeException("Failed to delete expense", ex);
        }
    }

    /** Total spend grouped by category — powers the dashboard pie chart. */
    public List<CategorySummary> summaryByCategory() {
        String sql = "SELECT category, SUM(amount) AS total, COUNT(*) AS cnt " +
                "FROM expenses GROUP BY category ORDER BY total DESC";
        List<CategorySummary> results = new ArrayList<>();
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                results.add(new CategorySummary(
                        rs.getString("category"),
                        rs.getDouble("total"),
                        rs.getLong("cnt")
                ));
            }
            return results;
        } catch (SQLException ex) {
            log.error("Failed to compute category summary", ex);
            throw new RuntimeException("Failed to compute category summary", ex);
        }
    }

    /** Grand total across all logged expenses. */
    public double grandTotal() {
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM expenses";
        try (Connection conn = db.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            return rs.next() ? rs.getDouble("total") : 0.0;
        } catch (SQLException ex) {
            log.error("Failed to compute grand total", ex);
            throw new RuntimeException("Failed to compute grand total", ex);
        }
    }

    private Expense mapRow(ResultSet rs) throws SQLException {
        return new Expense(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getDouble("amount"),
                rs.getString("category"),
                rs.getDate("expense_date").toLocalDate(),
                rs.getString("notes")
        );
    }
}
