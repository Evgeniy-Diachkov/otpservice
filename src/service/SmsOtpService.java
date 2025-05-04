package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class SmsOtpService {

    private static final String SMS_NOTIFICATION_FILE = "sms_notifications.txt";

    public void sendCode(String phoneNumber, String code) {
        try (PrintWriter out = new PrintWriter(new FileWriter(SMS_NOTIFICATION_FILE, true))) {
            out.println("SMS to " + phoneNumber + ": " + code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write SMS notification", e);
        }
    }
}
