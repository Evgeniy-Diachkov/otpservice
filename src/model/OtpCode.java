package model;

import java.time.LocalDateTime;

public record OtpCode(String operationId, String code, LocalDateTime createdAt, String status) {
}
