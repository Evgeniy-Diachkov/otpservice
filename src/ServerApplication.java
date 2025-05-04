import com.sun.net.httpserver.HttpServer;
import controller.AdminController;
import controller.AuthController;
import controller.UserController;
import dao.OtpCodeDao;
import dao.OtpConfigDao;
import dao.UserDao;
import service.EmailOtpService;
import service.FileOtpService;
import service.OtpExpirationScheduler;
import service.OtpService;
import service.OtpValidationService;
import service.SmsOtpService;
import service.TelegramOtpService;
import service.TokenService;
import util.LoggerUtil;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ServerApplication {

    public static void main(String[] args) {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

            // Инициализация DAO
            UserDao userDao = new UserDao();
            OtpCodeDao otpCodeDao = new OtpCodeDao();
            OtpConfigDao otpConfigDao = new OtpConfigDao();

            // Инициализация сервисов для отправки
            FileOtpService fileOtpService = new FileOtpService();
            SmsOtpService smsOtpService = new SmsOtpService();
            EmailOtpService emailOtpService = new EmailOtpService();
            TelegramOtpService telegramOtpService = new TelegramOtpService();

            // Бизнес-логика
            OtpService otpService = new OtpService(otpCodeDao, fileOtpService, otpConfigDao, smsOtpService, emailOtpService, telegramOtpService);
            OtpValidationService otpValidationService = new OtpValidationService(otpCodeDao);
            TokenService tokenService = new TokenService();

            // Инициализация контроллеров
            AuthController authController = new AuthController(userDao, tokenService);
            UserController userController = new UserController(otpService, otpValidationService);
            AdminController adminController = new AdminController(userDao, otpCodeDao, otpConfigDao, tokenService);

            // Регистрация эндпоинтов
            server.createContext("/auth", authController);
            server.createContext("/user/otp", userController);
            server.createContext("/admin", adminController);

            // Запуск планировщика экспирации
            startOtpExpirationScheduler(otpCodeDao);

            server.setExecutor(null);
            server.start();

            LoggerUtil.logInfo("Server started successfully on port 8080");

        } catch (IOException e) {
            LoggerUtil.logError("Failed to start the server", e);
        }
    }

    private static void startOtpExpirationScheduler(OtpCodeDao otpCodeDao) {
        int configuredLifetimeSeconds = 300; // Можно позже получать из БД
        OtpExpirationScheduler scheduler = new OtpExpirationScheduler(otpCodeDao, configuredLifetimeSeconds);
        scheduler.start();
    }
}
