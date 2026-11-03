package com.gmt.rental.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.http.Cookie;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path.startsWith("/api/public/") || path.equals("/api/auth/login")) return true;
        if (HttpMethod.GET.matches(request.getMethod())) {
            return path.startsWith("/api/vehicules/") || path.equals("/api/vehicules")
                    || path.startsWith("/api/categories/") || path.equals("/api/categories")
                    || path.startsWith("/api/parametres/") || path.equals("/api/parametres");
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = null;
        boolean fromCookie = false;
        if (header != null && header.startsWith("Bearer ")) {
            token = header.substring(7);
        } else {
            token = cookieToken(request);
            fromCookie = token != null;
        }
        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                if (jwtUtils.isValid(token)) {
                    UserDetails user = userDetailsService.loadUserByUsername(jwtUtils.getEmail(token));
                    if (user.isEnabled()) {
                        if (fromCookie && !isSafeMethod(request) && !hasCsrfHeader(request)) {
                            // Le cookie part automatiquement avec les requêtes inter-sites : on exige un en-tête
                            // personnalisé, que le navigateur n'envoie pas sans autorisation CORS explicite.
                            log.warn("Cookie-authenticated request rejected: missing {} header for {}", AuthCookies.CSRF_HEADER, request.getRequestURI());
                            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Requête refusée : en-tête de protection manquant.");
                            return;
                        }
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        log.warn("JWT authentication rejected because account is disabled: {}", user.getUsername());
                    }
                } else {
                    log.warn("JWT authentication rejected an invalid or expired token for {}", request.getRequestURI());
                }
            } catch (RuntimeException exception) {
                log.warn("JWT authentication failed for {}: {}", request.getRequestURI(), exception.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

    private static String cookieToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (AuthCookies.NAME.equals(cookie.getName()) && cookie.getValue() != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private static boolean isSafeMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method);
    }

    private static boolean hasCsrfHeader(HttpServletRequest request) {
        String value = request.getHeader(AuthCookies.CSRF_HEADER);
        return value != null && !value.isBlank();
    }
}
