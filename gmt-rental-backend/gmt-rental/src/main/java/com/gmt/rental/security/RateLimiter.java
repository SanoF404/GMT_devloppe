package com.gmt.rental.security;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limiteur en mémoire à fenêtre fixe, utilisé pour freiner les tentatives de connexion
 * et les envois massifs sur les routes publiques. Il protège une instance unique du serveur :
 * avec plusieurs instances, remplacez-le par un limiteur partagé (Redis, passerelle, etc.).
 */
public class RateLimiter {
    private static final int MAX_TRACKED_KEYS = 10_000;

    private record Window(long start, int count) {}

    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final long windowMillis;

    public RateLimiter(int maxAttempts, Duration window) {
        this.maxAttempts = maxAttempts;
        this.windowMillis = window.toMillis();
    }

    /** Enregistre une tentative et indique si elle reste dans la limite autorisée. */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        boolean[] allowed = {true};
        windows.compute(key, (k, current) -> {
            if (current == null || now - current.start() >= windowMillis) {
                return new Window(now, 1);
            }
            if (current.count() >= maxAttempts) {
                allowed[0] = false;
                return current;
            }
            return new Window(current.start(), current.count() + 1);
        });
        if (windows.size() > MAX_TRACKED_KEYS) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().start() >= windowMillis);
        }
        return allowed[0];
    }

    /** Indique si la limite est déjà atteinte, sans enregistrer de tentative. */
    public boolean isBlocked(String key) {
        Window current = windows.get(key);
        return current != null
                && System.currentTimeMillis() - current.start() < windowMillis
                && current.count() >= maxAttempts;
    }

    public void reset(String key) {
        windows.remove(key);
    }
}
