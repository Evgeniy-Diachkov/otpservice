package service;

import util.LoggerUtil;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;

public class FileOtpService {

    private static final String DIRECTORY_PATH = "generated-codes/";

    public FileOtpService() {
        createDirectoryIfNotExists();
    }

    private void createDirectoryIfNotExists() {
        try {
            Files.createDirectories(Paths.get(DIRECTORY_PATH));
            LoggerUtil.logInfo("Directory for OTP codes ensured at: " + DIRECTORY_PATH);
        } catch (IOException e) {
            LoggerUtil.logError("Failed to create directory for OTP codes", e);
            throw new RuntimeException("Could not create directory for OTP codes", e);
        }
    }

    public void saveCodeToFile(String otpCode, String operationId) {
        String filename = DIRECTORY_PATH + "otp_code_" + operationId + "_" + System.currentTimeMillis() + ".txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("Operation ID: " + operationId + System.lineSeparator());
            writer.write("OTP Code: " + otpCode + System.lineSeparator());
            writer.write("Generated At: " + LocalDateTime.now() + System.lineSeparator());
            LoggerUtil.logInfo("OTP code saved successfully to file: " + filename);
        } catch (IOException e) {
            LoggerUtil.logError("Failed to save OTP code to file: " + filename, e);
            throw new RuntimeException("Could not save OTP code to file", e);
        }
    }
}
