package com.gmt.rental.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Cookie de session administrateur : httpOnly, donc illisible par le JavaScript de la page. */
@Component
public class AuthCookies {
    public static final String NAME = "gmt_session";
    public static final String CSRF_HEADER = "X-Requested-With";

    private final boolean secure;
    private final String sameSite;
    private final Duration lifetime;

    public AuthCookies(
            @Value("${gmt.auth.cookie-secure:true}") boolean secure,
            @Value("${gmt.auth.cookie-same-site:None}") String sameSite,
            @Value("${gmt.jwt.expiration:28800000}") long expirationMs) {
        this.secure = secure;
        this.sameSite = sameSite;
        this.lifetime = Duration.ofMillis(expirationMs);
    }

    public String issue(String token) {
        return ResponseCookie.from(NAME, token).httpOnly(true).secure(secure).sameSite(sameSite)
                .path("/").maxAge(lifetime).build().toString();
    }

    public String clear() {
        return ResponseCookie.from(NAME, "").httpOnly(true).secure(secure).sameSite(sameSite)
                .path("/").maxAge(Duration.ZERO).build().toString();
    }
}
