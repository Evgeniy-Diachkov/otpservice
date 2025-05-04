package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class EmailOtpService {

    private static final String EMAIL_NOTIFICATION_FILE = "email_notifications.txt";

    public void sendCode(String email, String code) {
        try (PrintWriter out = new PrintWriter(new FileWriter(EMAIL_NOTIFICATION_FILE, true))) {
            out.println("Email to " + email + ": " + code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Email notification", e);
        }
    }
}
