package controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import service.OtpService;
import service.OtpValidationService;
import util.LoggerUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class UserController implements HttpHandler {

    private final OtpService otpService;
    private final OtpValidationService otpValidationService;

    public UserController(OtpService otpService, OtpValidationService otpValidationService) {
        this.otpService = otpService;
        this.otpValidationService = otpValidationService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            if (path.endsWith("/user/otp")) {
                handleGenerateOtp(exchange);
            } else if (path.endsWith("/user/validate")) {
                handleValidateOtp(exchange);
            } else {
                sendResponse(exchange, 404, "Not Found");
            }
        } else {
            sendResponse(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleGenerateOtp(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseQuery(requestBody);

        String operationId = params.get("operationId");
        String deliveryMethod = params.get("deliveryMethod");

        if (operationId == null || deliveryMethod == null) {
            sendResponse(exchange, 400, "Missing parameters: operationId and deliveryMethod are required");
            return;
        }

        try {
            String generatedCode = otpService.generateAndProcessOtpCode(operationId, deliveryMethod);
            sendResponse(exchange, 200, "OTP code generated successfully: " + generatedCode);
        } catch (Exception e) {
            LoggerUtil.logError("Error generating OTP code", e);
            sendResponse(exchange, 500, "Error generating OTP code: " + e.getMessage());
        }
    }

    private void handleValidateOtp(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> params = parseQuery(requestBody);

        String operationId = params.get("operationId");
        String otpCode = params.get("otpCode");

        if (operationId == null || otpCode == null) {
            sendResponse(exchange, 400, "Missing parameters: operationId and otpCode are required");
            return;
        }

        boolean isValid = otpValidationService.validateOtp(operationId, otpCode, 300);

        if (isValid) {
            sendResponse(exchange, 200, "OTP code validated successfully");
        } else {
            sendResponse(exchange, 400, "Invalid or expired OTP code");
        }
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
