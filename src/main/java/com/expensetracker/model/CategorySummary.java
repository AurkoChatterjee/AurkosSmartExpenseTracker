package com.expensetracker.model;

/**
 * Aggregated total spend for a single category — used to feed the
 * dashboard's pie chart and summary cards.
 */
public class CategorySummary {

    private String category;
    private double total;
    private long count;

    public CategorySummary() {
    }

    public CategorySummary(String category, double total, long count) {
        this.category = category;
        this.total = total;
        this.count = count;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
