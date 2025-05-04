package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class TelegramOtpService {

    private static final String TELEGRAM_NOTIFICATION_FILE = "telegram_notifications.txt";

    public void sendCode(String telegramId, String code) {
        try (PrintWriter out = new PrintWriter(new FileWriter(TELEGRAM_NOTIFICATION_FILE, true))) {
            out.println("Telegram to " + telegramId + ": " + code);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write Telegram notification", e);
        }
    }
}
