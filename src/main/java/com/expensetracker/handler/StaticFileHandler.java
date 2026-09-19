package com.expensetracker.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Minimal static file server for the frontend/ folder — good enough for a
 * local resume/demo project, not meant to replace a real web server.
 */
public class StaticFileHandler implements HttpHandler {

    private final Path root;

    public StaticFileHandler(String rootDir) {
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        Path filePath = root.resolve(requestPath.substring(1)).normalize();

        // Prevent path traversal outside the frontend directory
        if (!filePath.startsWith(root) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
            byte[] body = "404 Not Found".getBytes();
            exchange.sendResponseHeaders(404, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
            return;
        }

        exchange.getResponseHeaders().add("Content-Type", contentTypeFor(filePath));
        byte[] bytes = Files.readAllBytes(filePath);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private String contentTypeFor(Path path) {
        String name = path.toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
        if (name.endsWith(".json")) return "application/json; charset=utf-8";
        return "application/octet-stream";
    }
}
