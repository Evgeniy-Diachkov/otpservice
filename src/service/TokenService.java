package service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TokenService {

    private final Map<String, LocalDateTime> tokens = new HashMap<>();
    private final int tokenLifetimeMinutes = 15; // Токен живёт 15 минут

    public String generateToken(String username) {
        String token = UUID.randomUUID().toString();
        tokens.put(token, LocalDateTime.now().plusMinutes(tokenLifetimeMinutes));
        return token;
    }

    public boolean validateToken(String token) {
        LocalDateTime expiry = tokens.get(token);
        if (expiry == null) {
            return false;
        }
        if (LocalDateTime.now().isAfter(expiry)) {
            tokens.remove(token); // Сразу очищаем протухший токен
            return false;
        }
        return true;
    }
}
