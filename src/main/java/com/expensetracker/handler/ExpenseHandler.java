package com.expensetracker.handler;

import com.expensetracker.db.ExpenseDao;
import com.expensetracker.model.CategorySummary;
import com.expensetracker.model.Expense;
import com.expensetracker.util.JsonUtil;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Routes every request under /api/expenses to the right CRUD or
 * aggregation operation. Deliberately hand-rolled (no Spring MVC) so the
 * request lifecycle is fully visible for a viva walkthrough.
 *
 * Routes:
 *   GET    /api/expenses            -> list all expenses
 *   POST   /api/expenses            -> create a new expense
 *   GET    /api/expenses/{id}       -> fetch one expense
 *   PUT    /api/expenses/{id}       -> update an expense
 *   DELETE /api/expenses/{id}       -> delete an expense
 *   GET    /api/expenses/summary    -> category totals + grand total
 */
public class ExpenseHandler implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(ExpenseHandler.class);
    private static final String BASE_PATH = "/api/expenses";

    private final ExpenseDao dao;

    public ExpenseHandler(ExpenseDao dao) {
        this.dao = dao;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // Allow the plain HTML/JS frontend to be opened from anywhere (file://, live-server, etc.)
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        try {
            if ("OPTIONS".equalsIgnoreCase(method)) {
                sendEmpty(exchange, 204);
                return;
            }

            String remainder = path.substring(BASE_PATH.length()); // "", "/", "/5", "/summary"

            if (remainder.isEmpty() || remainder.equals("/")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleListAll(exchange);
                } else if ("POST".equalsIgnoreCase(method)) {
                    handleCreate(exchange);
                } else {
                    sendJson(exchange, 405, Map.of("error", "Method not allowed"));
                }
                return;
            }

            String segment = remainder.startsWith("/") ? remainder.substring(1) : remainder;

            if (segment.equals("summary")) {
                if ("GET".equalsIgnoreCase(method)) {
                    handleSummary(exchange);
                } else {
                    sendJson(exchange, 405, Map.of("error", "Method not allowed"));
                }
                return;
            }

            // Otherwise expect a numeric id
            long id;
            try {
                id = Long.parseLong(segment);
            } catch (NumberFormatException nfe) {
                sendJson(exchange, 404, Map.of("error", "Not found"));
                return;
            }

            switch (method.toUpperCase()) {
                case "GET" -> handleGetOne(exchange, id);
                case "PUT" -> handleUpdate(exchange, id);
                case "DELETE" -> handleDelete(exchange, id);
                default -> sendJson(exchange, 405, Map.of("error", "Method not allowed"));
            }

        } catch (Exception e) {
            log.error("Unhandled error processing {} {}", method, path, e);
            sendJson(exchange, 500, Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    private void handleListAll(HttpExchange exchange) throws IOException {
        List<Expense> expenses = dao.findAll();
        sendJson(exchange, 200, expenses);
    }

    private void handleCreate(HttpExchange exchange) throws IOException {
        Expense incoming = readBody(exchange, Expense.class);
        if (incoming == null || incoming.getTitle() == null || incoming.getTitle().isBlank()
                || incoming.getCategory() == null || incoming.getCategory().isBlank()
                || incoming.getDate() == null || incoming.getAmount() <= 0) {
            sendJson(exchange, 400, Map.of("error", "title, category, date and a positive amount are required"));
            return;
        }
        Expense created = dao.create(incoming);
        sendJson(exchange, 201, created);
    }

    private void handleGetOne(HttpExchange exchange, long id) throws IOException {
        Expense found = dao.findById(id);
        if (found == null) {
            sendJson(exchange, 404, Map.of("error", "Expense " + id + " not found"));
            return;
        }
        sendJson(exchange, 200, found);
    }

    private void handleUpdate(HttpExchange exchange, long id) throws IOException {
        Expense incoming = readBody(exchange, Expense.class);
        if (incoming == null || incoming.getTitle() == null || incoming.getTitle().isBlank()
                || incoming.getCategory() == null || incoming.getCategory().isBlank()
                || incoming.getDate() == null || incoming.getAmount() <= 0) {
            sendJson(exchange, 400, Map.of("error", "title, category, date and a positive amount are required"));
            return;
        }
        boolean updated = dao.update(id, incoming);
        if (!updated) {
            sendJson(exchange, 404, Map.of("error", "Expense " + id + " not found"));
            return;
        }
        incoming.setId(id);
        sendJson(exchange, 200, incoming);
    }

    private void handleDelete(HttpExchange exchange, long id) throws IOException {
        boolean deleted = dao.delete(id);
        if (!deleted) {
            sendJson(exchange, 404, Map.of("error", "Expense " + id + " not found"));
            return;
        }
        sendEmpty(exchange, 204);
    }

    private void handleSummary(HttpExchange exchange) throws IOException {
        List<CategorySummary> byCategory = dao.summaryByCategory();
        double grandTotal = dao.grandTotal();

        Map<String, Object> response = new HashMap<>();
        response.put("byCategory", byCategory);
        response.put("grandTotal", grandTotal);
        sendJson(exchange, 200, response);
    }

    private <T> T readBody(HttpExchange exchange, Class<T> type) throws IOException {
        try (InputStream is = exchange.getRequestBody()) {
            byte[] bytes = is.readAllBytes();
            if (bytes.length == 0) {
                return null;
            }
            return JsonUtil.MAPPER.readValue(bytes, type);
        }
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object body) throws IOException {
        byte[] bytes = JsonUtil.MAPPER.writeValueAsBytes(body);
        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendEmpty(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }
}
