package com.expensetracker;

import com.expensetracker.db.DatabaseManager;
import com.expensetracker.db.ExpenseDao;
import com.expensetracker.handler.ExpenseHandler;
import com.expensetracker.handler.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);
    private static final int PORT = 8080;

    public static void main(String[] args) throws Exception {
        DatabaseManager db = new DatabaseManager();
        db.initSchema();

        ExpenseDao dao = new ExpenseDao(db);

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/expenses", new ExpenseHandler(dao));
        server.createContext("/", new StaticFileHandler("frontend"));
        server.setExecutor(Executors.newFixedThreadPool(8));

        server.start();
        log.info("Smart Expense Tracker running at http://localhost:{}", PORT);
        System.out.println("Smart Expense Tracker running at http://localhost:" + PORT);
    }
}
