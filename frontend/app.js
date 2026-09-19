const API_BASE = "/api/expenses";

const form = document.getElementById("expense-form");
const idField = document.getElementById("expense-id");
const titleField = document.getElementById("title");
const amountField = document.getElementById("amount");
const categoryField = document.getElementById("category");
const dateField = document.getElementById("date");
const notesField = document.getElementById("notes");
const formError = document.getElementById("form-error");
const formTitle = document.getElementById("form-title");
const submitBtn = document.getElementById("submit-btn");
const cancelEditBtn = document.getElementById("cancel-edit-btn");

const tbody = document.getElementById("expense-tbody");
const tableEmpty = document.getElementById("table-empty");
const chartEmpty = document.getElementById("chart-empty");
const grandTotalEl = document.getElementById("grand-total");
const entryCountEl = document.getElementById("entry-count");
const topCategoryEl = document.getElementById("top-category");

let categoryChart = null;

const CATEGORY_COLORS = {
    Food: "#f97316",
    Rent: "#4f46e5",
    Entertainment: "#ec4899",
    Travel: "#0ea5e9",
    Utilities: "#22c55e",
    Health: "#ef4444",
    Shopping: "#a855f7",
    Other: "#6b7280"
};

document.addEventListener("DOMContentLoaded", () => {
    dateField.valueAsDate = new Date();
    loadExpenses();
    loadSummary();
});

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    formError.classList.add("hidden");

    const payload = {
        title: titleField.value.trim(),
        amount: parseFloat(amountField.value),
        category: categoryField.value,
        date: dateField.value,
        notes: notesField.value.trim()
    };

    const editingId = idField.value;
    const url = editingId ? `${API_BASE}/${editingId}` : API_BASE;
    const method = editingId ? "PUT" : "POST";

    try {
        const res = await fetch(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        });

        if (!res.ok) {
            const err = await safeJson(res);
            throw new Error(err?.error || `Request failed (${res.status})`);
        }

        resetForm();
        await loadExpenses();
        await loadSummary();
    } catch (err) {
        formError.textContent = err.message;
        formError.classList.remove("hidden");
    }
});

cancelEditBtn.addEventListener("click", resetForm);

async function loadExpenses() {
    const res = await fetch(API_BASE);
    const expenses = await res.json();
    renderTable(expenses);
}

async function loadSummary() {
    const res = await fetch(`${API_BASE}/summary`);
    const summary = await res.json();
    renderSummary(summary);
}

function renderTable(expenses) {
    tbody.innerHTML = "";

    if (expenses.length === 0) {
        tableEmpty.classList.remove("hidden");
        return;
    }
    tableEmpty.classList.add("hidden");

    for (const exp of expenses) {
        const tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${exp.date}</td>
            <td>${escapeHtml(exp.title)}</td>
            <td>${escapeHtml(exp.category)}</td>
            <td class="amount-cell">₹${exp.amount.toFixed(2)}</td>
            <td>${escapeHtml(exp.notes || "")}</td>
            <td class="row-actions">
                <button class="edit-btn" data-id="${exp.id}">Edit</button>
                <button class="delete-btn" data-id="${exp.id}">Delete</button>
            </td>
        `;
        tbody.appendChild(tr);
    }

    tbody.querySelectorAll(".edit-btn").forEach((btn) =>
        btn.addEventListener("click", () => startEdit(btn.dataset.id, expenses))
    );
    tbody.querySelectorAll(".delete-btn").forEach((btn) =>
        btn.addEventListener("click", () => deleteExpense(btn.dataset.id))
    );
}

function startEdit(id, expenses) {
    const exp = expenses.find((e) => String(e.id) === String(id));
    if (!exp) return;

    idField.value = exp.id;
    titleField.value = exp.title;
    amountField.value = exp.amount;
    categoryField.value = exp.category;
    dateField.value = exp.date;
    notesField.value = exp.notes || "";

    formTitle.textContent = "Edit Expense";
    submitBtn.textContent = "Save Changes";
    cancelEditBtn.classList.remove("hidden");
    window.scrollTo({ top: 0, behavior: "smooth" });
}

async function deleteExpense(id) {
    if (!confirm("Delete this expense?")) return;

    const res = await fetch(`${API_BASE}/${id}`, { method: "DELETE" });
    if (res.ok || res.status === 204) {
        await loadExpenses();
        await loadSummary();
    }
}

function resetForm() {
    form.reset();
    idField.value = "";
    dateField.valueAsDate = new Date();
    formTitle.textContent = "Add Expense";
    submitBtn.textContent = "Add Expense";
    cancelEditBtn.classList.add("hidden");
    formError.classList.add("hidden");
}

function renderSummary(summary) {
    const byCategory = summary.byCategory || [];
    const grandTotal = summary.grandTotal || 0;

    grandTotalEl.textContent = `₹${grandTotal.toFixed(2)}`;
    entryCountEl.textContent = byCategory.reduce((sum, c) => sum + c.count, 0);
    topCategoryEl.textContent = byCategory.length > 0 ? byCategory[0].category : "—";

    renderChart(byCategory);
}

function renderChart(byCategory) {
    const canvas = document.getElementById("category-chart");

    if (byCategory.length === 0) {
        canvas.classList.add("hidden");
        chartEmpty.classList.remove("hidden");
        if (categoryChart) {
            categoryChart.destroy();
            categoryChart = null;
        }
        return;
    }

    canvas.classList.remove("hidden");
    chartEmpty.classList.add("hidden");

    const labels = byCategory.map((c) => c.category);
    const data = byCategory.map((c) => c.total);
    const colors = labels.map((label, i) => CATEGORY_COLORS[label] || defaultColor(i));

    if (categoryChart) {
        categoryChart.data.labels = labels;
        categoryChart.data.datasets[0].data = data;
        categoryChart.data.datasets[0].backgroundColor = colors;
        categoryChart.update();
        return;
    }

    categoryChart = new Chart(canvas, {
        type: "pie",
        data: {
            labels,
            datasets: [{ data, backgroundColor: colors }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { position: "bottom" }
            }
        }
    });
}

function defaultColor(i) {
    const palette = ["#4f46e5", "#0ea5e9", "#22c55e", "#f97316", "#ec4899", "#a855f7"];
    return palette[i % palette.length];
}

function escapeHtml(str) {
    const div = document.createElement("div");
    div.textContent = str;
    return div.innerHTML;
}

async function safeJson(res) {
    try {
        return await res.json();
    } catch {
        return null;
    }
}
