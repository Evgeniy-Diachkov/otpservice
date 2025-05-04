package service;

import dao.OtpCodeDao;
import dao.OtpConfigDao;
import model.OtpCode;
import util.CodeGenerator;

import java.time.LocalDateTime;

public class OtpService {

    private final OtpCodeDao otpCodeDao;
    private final FileOtpService fileOtpService;
    private final OtpConfigDao otpConfigDao;
    private final SmsOtpService smsOtpService;
    private final EmailOtpService emailOtpService;
    private final TelegramOtpService telegramOtpService;

    public OtpService(OtpCodeDao otpCodeDao,
                      FileOtpService fileOtpService,
                      OtpConfigDao otpConfigDao,
                      SmsOtpService smsOtpService,
                      EmailOtpService emailOtpService,
                      TelegramOtpService telegramOtpService) {
        this.otpCodeDao = otpCodeDao;
        this.fileOtpService = fileOtpService;
        this.otpConfigDao = otpConfigDao;
        this.smsOtpService = smsOtpService;
        this.emailOtpService = emailOtpService;
        this.telegramOtpService = telegramOtpService;
    }

    public String generateAndProcessOtpCode(String operationId, String deliveryMethod) {
        // Получаем настройки (длина кода)
        int codeLength = otpConfigDao.getConfig().codeLength();
        String otpCode = CodeGenerator.generateNumericCode(codeLength);

        OtpCode newOtpCode = new OtpCode(
                operationId,
                otpCode,
                LocalDateTime.now(),
                "ACTIVE"
        );
        otpCodeDao.save(newOtpCode);

        switch (deliveryMethod.toUpperCase()) {
            case "SMS":
                smsOtpService.sendCode(operationId, otpCode);
                break;
            case "EMAIL":
                emailOtpService.sendCode(operationId, otpCode);
                break;
            case "TELEGRAM":
                telegramOtpService.sendCode(operationId, otpCode);
                break;
            case "FILE":
                fileOtpService.saveCodeToFile(otpCode, operationId);
                break;
            default:
                throw new IllegalArgumentException("Unsupported delivery method: " + deliveryMethod);
        }

        return otpCode;
    }
}
