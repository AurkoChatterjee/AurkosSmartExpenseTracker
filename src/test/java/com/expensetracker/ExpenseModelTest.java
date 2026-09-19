package com.expensetracker;

import com.expensetracker.model.CategorySummary;
import com.expensetracker.model.Expense;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Lightweight unit tests for the model classes — no DB required, so these
 * run anywhere (including CI without a MySQL instance available).
 */
class ExpenseModelTest {

    @Test
    void expenseGettersAndSettersRoundTrip() {
        Expense e = new Expense(1L, "Groceries", 499.50, "Food", LocalDate.of(2026, 9, 1), "Weekly shop");

        assertEquals(1L, e.getId());
        assertEquals("Groceries", e.getTitle());
        assertEquals(499.50, e.getAmount());
        assertEquals("Food", e.getCategory());
        assertEquals(LocalDate.of(2026, 9, 1), e.getDate());
        assertEquals("Weekly shop", e.getNotes());
    }

    @Test
    void expenseNoArgConstructorAllowsFieldByFieldSetup() {
        Expense e = new Expense();
        e.setTitle("Movie night");
        e.setAmount(650.0);
        e.setCategory("Entertainment");
        e.setDate(LocalDate.of(2026, 9, 5));

        assertEquals("Movie night", e.getTitle());
        assertEquals(650.0, e.getAmount());
        assertEquals("Entertainment", e.getCategory());
    }

    @Test
    void categorySummaryHoldsAggregatedValues() {
        CategorySummary summary = new CategorySummary("Food", 1250.75, 4);

        assertEquals("Food", summary.getCategory());
        assertEquals(1250.75, summary.getTotal());
        assertEquals(4, summary.getCount());
    }
}
