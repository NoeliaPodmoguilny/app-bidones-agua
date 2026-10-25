package com.agua.bidones.security;

import com.agua.bidones.exception.TooManyAttemptsException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tras varios intentos de ingreso fallidos se bloquea el login por un momento.
 *  - por IP + usuario: 5 fallos en 15 minutos;
 *  - por usuario (sin importar la IP): 20 fallos en 15 minutos, para frenar ataques desde muchas IP.
 * Los contadores viven en memoria y se reinician al reiniciar la app.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_PER_IP_AND_USER = 5;
    private static final int MAX_PER_USER = 20;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int MAX_REGISTRATIONS_PER_HOUR = 5;
    private static final Duration REGISTRATION_WINDOW = Duration.ofHours(1);
    private static final int CLEANUP_THRESHOLD = 5000;

    private record Attempts(int count, Instant windowStart, Duration window) {
        boolean expired(Instant now) {
            return windowStart.plus(window).isBefore(now);
        }
    }

    private final ConcurrentHashMap<String, Attempts> attempts = new ConcurrentHashMap<>();

    private static String userKey(String identifier) {
        return "u:" + (identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT));
    }

    private static String ipUserKey(String ip, String identifier) {
        return "i:" + ip + ":" + (identifier == null ? "" : identifier.trim().toLowerCase(Locale.ROOT));
    }

    /** Lanza TooManyAttemptsException si el login está bloqueado. */
    public void checkAllowed(String ip, String identifier) {
        Instant now = Instant.now();
        long wait = Math.max(
                remainingSeconds(ipUserKey(ip, identifier), MAX_PER_IP_AND_USER, now),
                remainingSeconds(userKey(identifier), MAX_PER_USER, now));
        if (wait > 0) {
            throw new TooManyAttemptsException(wait);
        }
    }

    public void recordFailure(String ip, String identifier) {
        if (attempts.size() > CLEANUP_THRESHOLD) {
            Instant now = Instant.now();
            attempts.entrySet().removeIf(entry -> entry.getValue().expired(now));
        }
        increment(ipUserKey(ip, identifier));
        increment(userKey(identifier));
    }

    /**
     * Alta pública de cuentas de admin: máximo 5 por hora desde la misma IP (frena el spam de cuentas).
     */
    public void checkRegistrationAllowed(String ip) {
        String key = "r:" + ip;
        Instant now = Instant.now();
        long wait = remainingSeconds(key, MAX_REGISTRATIONS_PER_HOUR, now);
        if (wait > 0) {
            throw new TooManyAttemptsException(wait);
        }
        increment(key, REGISTRATION_WINDOW);
    }

    /** IP del cliente */
    public static String clientIp(jakarta.servlet.http.HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    public void recordSuccess(String ip, String identifier) {
        attempts.remove(ipUserKey(ip, identifier));
    }

    private void increment(String key) {
        increment(key, WINDOW);
    }

    private void increment(String key, Duration window) {
        Instant now = Instant.now();
        attempts.compute(key, (k, current) ->
                (current == null || current.expired(now))
                        ? new Attempts(1, now, window)
                        : new Attempts(current.count() + 1, current.windowStart(), current.window()));
    }

    private long remainingSeconds(String key, int max, Instant now) {
        Attempts current = attempts.get(key);
        if (current == null || current.expired(now) || current.count() < max) {
            return 0;
        }
        return Math.max(1, Duration.between(now, current.windowStart().plus(current.window())).getSeconds());
    }
}
