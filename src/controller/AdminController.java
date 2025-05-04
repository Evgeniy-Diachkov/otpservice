package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.OtpCodeDao;
import dao.OtpConfigDao;
import dao.UserDao;
import model.OtpConfig;
import model.User;
import service.TokenService;
import util.LoggerUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AdminController implements HttpHandler {

    private final UserDao userDao;
    private final OtpCodeDao otpCodeDao;
    private final OtpConfigDao otpConfigDao;
    private final TokenService tokenService;

    public AdminController(UserDao userDao, OtpCodeDao otpCodeDao, OtpConfigDao otpConfigDao, TokenService tokenService) {
        this.userDao = userDao;
        this.otpCodeDao = otpCodeDao;
        this.otpConfigDao = otpConfigDao;
        this.tokenService = tokenService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!isAdminAuthorized(exchange)) {
            sendResponse(exchange, 403, "Access denied. Admins only.");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        if (path.endsWith("/otp-config") && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleUpdateOtpConfig(exchange);
        } else if (path.endsWith("/users") && "GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleGetUsers(exchange);
        } else if (path.startsWith("/admin/users/") && "DELETE".equalsIgnoreCase(exchange.getRequestMethod())) {
            handleDeleteUser(exchange);
        } else {
            sendResponse(exchange, 404, "Not Found");
        }
    }

    private boolean isAdminAuthorized(HttpExchange exchange) {
        String token = exchange.getRequestHeaders().getFirst("Authorization");
        return token != null && tokenService.validateToken(token);
    }

    private void handleUpdateOtpConfig(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseQuery(body);

        int codeLength = Integer.parseInt(params.getOrDefault("codeLength", "6"));
        int codeLifetime = Integer.parseInt(params.getOrDefault("codeLifetimeSeconds", "300"));

        OtpConfig config = new OtpConfig(codeLength, codeLifetime);
        otpConfigDao.saveOrUpdate(config);

        LoggerUtil.logInfo("OTP configuration updated by admin.");
        sendResponse(exchange, 200, "OTP configuration updated.");
    }

    private void handleGetUsers(HttpExchange exchange) throws IOException {
        List<User> users = userDao.findAllUsersExceptAdmins();

        String response = users.stream()
                .map(User::username)
                .collect(Collectors.joining(", "));

        LoggerUtil.logInfo("Admin retrieved user list.");
        sendResponse(exchange, 200, response.isEmpty() ? "No users found" : response);
    }

    private void handleDeleteUser(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] parts = path.split("/");
        if (parts.length != 4) {
            sendResponse(exchange, 400, "Invalid request");
            return;
        }

        String username = parts[3];

        userDao.deleteByUsername(username);
        otpCodeDao.deleteByOperationIdPattern(username);

        LoggerUtil.logInfo("Admin deleted user: " + username);
        sendResponse(exchange, 200, "User and related OTP codes deleted successfully.");
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
