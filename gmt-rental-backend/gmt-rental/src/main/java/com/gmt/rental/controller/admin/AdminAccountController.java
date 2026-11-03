package com.gmt.rental.controller.admin;

import com.gmt.rental.dto.admin.AccountDtos.*;
import com.gmt.rental.service.admin.AdminAccountService;
import com.gmt.rental.service.admin.AdminActivityService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.gmt.rental.service.EmailService;
import com.gmt.rental.service.QuoteEmailNotificationService;

@RestController
@RequestMapping("/api/admin/account")
public class AdminAccountController {
    private final AdminAccountService accounts;
    private final AdminActivityService activities;
    private final EmailService emails;
    private final ConcurrentMap<String, Long> testAttempts = new ConcurrentHashMap<>();
    private final com.gmt.rental.security.AuthCookies cookies;
    public AdminAccountController(AdminAccountService accounts, AdminActivityService activities, EmailService emails, com.gmt.rental.security.AuthCookies cookies) { this.accounts = accounts; this.activities = activities; this.emails = emails; this.cookies = cookies; }

    @GetMapping("/profile") public ProfileResponse profile(Authentication auth) { return accounts.profile(auth.getName()); }
    @PutMapping("/profile") public ProfileResponse updateProfile(Authentication auth, @RequestBody ProfileUpdate body) { return accounts.updateProfile(auth.getName(), body); }
    @PostMapping("/email/send-code") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendEmailCode(Authentication auth, @RequestBody EmailChange body) { accounts.sendEmailCode(auth.getName(), body); }
    @PostMapping("/email/verify")
    public ResponseEntity<EmailChangeResult> verifyEmail(Authentication auth, @RequestBody EmailCodeVerification body) {
        EmailChangeResult result = accounts.confirmEmailChange(auth.getName(), body);
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookies.issue(result.token())).body(result);
    }
    @PutMapping("/password") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(Authentication auth, @RequestBody PasswordChange body) { accounts.changePassword(auth.getName(), body); }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(Authentication auth) {
        activities.record(auth.getName(), "", "LOGOUT", "security", "Déconnexion administrateur", true);
        return ResponseEntity.noContent().header(HttpHeaders.SET_COOKIE, cookies.clear()).build();
    }
    @GetMapping("/preferences") public PreferencesResponse preferences(Authentication auth) { return accounts.getPreferences(auth.getName()); }
    @PutMapping("/preferences") public PreferencesResponse updatePreferences(Authentication auth, @RequestBody PreferencesUpdate body) { return accounts.updatePreferences(auth.getName(), body); }

    @PostMapping("/email-test")
    public EmailTestResponse sendEmailTest(Authentication auth, @RequestBody EmailTestRequest body) {
        if (body == null || !QuoteEmailNotificationService.validEmail(body.email()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saisissez une adresse e-mail valide.");
        long now = System.currentTimeMillis();
        Long previous = testAttempts.putIfAbsent(auth.getName(), now);
        if (previous != null && now - previous < 60_000) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Attendez une minute avant de relancer un test.");
        }
        testAttempts.put(auth.getName(), now);
        try {
            String messageId = emails.send(body.email().trim(), "Test d’envoi — Gestion Rental",
                    "<div style='font-family:Arial,sans-serif'><h1>Test d’envoi d’e-mail</h1><p>Le service e-mail Gestion Rental est correctement configuré et Resend a accepté ce message.</p></div>",
                    "Test d’envoi d’e-mail : Resend a accepté ce message pour Gestion Rental.");
            return new EmailTestResponse("ACCEPTED", "Resend a accepté l’e-mail de test. Cela ne garantit pas encore sa livraison dans la boîte de réception.", messageId);
        } catch (EmailService.EmailNotConfiguredException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
        } catch (EmailService.EmailProviderException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "L’envoi a échoué. Vérifiez la clé API, l’expéditeur et le domaine autorisé dans Resend.");
        }
    }
}
