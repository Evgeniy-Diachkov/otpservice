package service;

import dao.OtpCodeDao;
import model.OtpCode;
import util.LoggerUtil;

import java.time.Duration;
import java.time.LocalDateTime;

public class OtpValidationService {

    private final OtpCodeDao otpCodeDao;
    private final int defaultLifetimeSeconds = 300; // 5 минут, если нет конфигурации

    public OtpValidationService(OtpCodeDao otpCodeDao) {
        this.otpCodeDao = otpCodeDao;
    }

    public boolean validateOtp(String operationId, String inputCode, int configuredLifetime) {
        OtpCode otpCode = otpCodeDao.findByOperationId(operationId);

        if (otpCode == null) {
            LoggerUtil.logWarning("Validation failed: OTP code not found for operationId=" + operationId);
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(otpCode.createdAt(), now);

        int lifetime = configuredLifetime > 0 ? configuredLifetime : defaultLifetimeSeconds;

        if (duration.getSeconds() > lifetime) {
            otpCodeDao.updateStatus(operationId, "EXPIRED");
            LoggerUtil.logWarning("Validation failed: OTP code expired for operationId=" + operationId);
            return false;
        }

        if (otpCode.code().equals(inputCode) && "ACTIVE".equals(otpCode.status())) {
            otpCodeDao.updateStatus(operationId, "USED");
            LoggerUtil.logInfo("OTP code validated and marked as USED for operationId=" + operationId);
            return true;
        }

        LoggerUtil.logWarning("Validation failed: invalid code for operationId=" + operationId);
        return false;
    }
}
