package service;

import dao.OtpCodeDao;
import model.OtpCode;
import util.LoggerUtil;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class OtpExpirationScheduler {

    private final OtpCodeDao otpCodeDao;
    private final int configuredLifetimeSeconds;

    public OtpExpirationScheduler(OtpCodeDao otpCodeDao, int configuredLifetimeSeconds) {
        this.otpCodeDao = otpCodeDao;
        this.configuredLifetimeSeconds = configuredLifetimeSeconds;
    }

    public void start() {
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(new ExpirationTask(), 0, 60000); // каждые 60 секунд
        LoggerUtil.logInfo("OTP Expiration Scheduler started");
    }

    class ExpirationTask extends TimerTask {
        @Override
        public void run() {
            List<OtpCode> activeCodes = otpCodeDao.findAllActiveCodes();
            LocalDateTime now = LocalDateTime.now();

            for (OtpCode code : activeCodes) {
                Duration duration = Duration.between(code.createdAt(), now);

                if (duration.getSeconds() > configuredLifetimeSeconds) {
                    otpCodeDao.updateStatus(code.operationId(), "EXPIRED");
                    LoggerUtil.logInfo("OTP code expired for operationId=" + code.operationId());
                }
            }
        }
    }
}
