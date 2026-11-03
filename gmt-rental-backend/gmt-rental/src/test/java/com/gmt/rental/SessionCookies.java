package com.gmt.rental;

import org.springframework.test.web.servlet.MvcResult;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Extrait le jeton de session depuis le cookie httpOnly renvoyé par la connexion. */
final class SessionCookies {
    private static final Pattern SESSION_COOKIE = Pattern.compile("gmt_session=([^;]+)");

    private SessionCookies() {}

    static String sessionToken(MvcResult result) {
        String setCookie = result.getResponse().getHeader("Set-Cookie");
        assertTrue(setCookie != null, "La connexion doit renvoyer un cookie de session.");
        assertTrue(setCookie.contains("HttpOnly"), "Le cookie de session doit être httpOnly.");
        Matcher matcher = SESSION_COOKIE.matcher(setCookie);
        assertTrue(matcher.find(), "Le cookie de session gmt_session doit être présent.");
        return matcher.group(1);
    }
}
