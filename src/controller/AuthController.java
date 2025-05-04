package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UserDao;
import model.User;
import service.TokenService;
import util.LoggerUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AuthController implements HttpHandler {

    private final UserDao userDao;
    private final TokenService tokenService;

    public AuthController(UserDao userDao, TokenService tokenService) {
        this.userDao = userDao;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();

            if (path.endsWith("/register")) {
                handleRegister(exchange);
            } else if (path.endsWith("/login")) {
                handleLogin(exchange);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseQuery(body);

        String username = params.get("username");
        String password = params.get("password");
        String role = params.get("role");

        if (username == null || password == null || role == null) {
            sendResponse(exchange, 400, "Missing parameters: username, password, role are required");
            return;
        }

        if ("ADMIN".equalsIgnoreCase(role) && userDao.adminExists()) {
            sendResponse(exchange, 403, "Admin already exists");
            return;
        }

        userDao.save(new User(username, password, role.toUpperCase()));
        LoggerUtil.logInfo("User registered: " + username);
        sendResponse(exchange, 200, "User registered successfully");
    }

    private void handleLogin(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseQuery(body);

        String username = params.get("username");
        String password = params.get("password");

        if (username == null || password == null) {
            sendResponse(exchange, 400, "Missing parameters: username and password are required");
            return;
        }

        User user = userDao.findByUsername(username);
        if (user == null || !user.passwordHash().equals(password)) {
            sendResponse(exchange, 401, "Invalid username or password");
            return;
        }

        String token = tokenService.generateToken(username);
        LoggerUtil.logInfo("User logged in: " + username + ", token generated");
        sendResponse(exchange, 200, "Login successful. Token: " + token);
    }

    private Map<String, String> parseQuery(String query) {
        return Map.ofEntries(
                java.util.Arrays.stream(query.split("&"))
                        .map(param -> param.split("="))
                        .filter(pair -> pair.length == 2)
                        .map(pair -> Map.entry(pair[0], pair[1]))
                        .toArray(Map.Entry[]::new)
        );
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        exchange.sendResponseHeaders(statusCode, responseText.getBytes().length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseText.getBytes());
        }
    }
}
