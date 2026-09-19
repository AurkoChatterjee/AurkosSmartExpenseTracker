package com.expensetracker.model;

import java.time.LocalDate;

/**
 * Represents a single expense entry logged by the user.
 */
public class Expense {

    private Long id;
    private String title;
    private double amount;
    private String category;   // e.g. Food, Rent, Entertainment, Travel, Utilities, Other
    private LocalDate date;
    private String notes;

    public Expense() {
        // Needed for Jackson deserialization
    }

    public Expense(Long id, String title, double amount, String category, LocalDate date, String notes) {
        this.id = id;
        this.title = title;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
