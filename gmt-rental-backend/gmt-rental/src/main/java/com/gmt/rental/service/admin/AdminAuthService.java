package com.gmt.rental.service.admin;

import com.gmt.rental.dto.admin.LoginRequest;
import com.gmt.rental.dto.admin.LoginResponse;
import com.gmt.rental.entity.User;
import com.gmt.rental.repository.UserRepository;
import com.gmt.rental.security.JwtUtils;
import com.gmt.rental.security.RateLimiter;
import com.gmt.rental.service.admin.AdminActivityService;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminAuthService {
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AdminActivityService activities;
    // 5 échecs par couple (e-mail, adresse IP) sur 15 minutes : une personne qui tape n'importe quoi depuis
    // une autre connexion ne peut donc pas verrouiller le compte de l'administrateur.
    private final RateLimiter failedLogins = new RateLimiter(5, Duration.ofMinutes(15));
    // Plafond par adresse IP, toutes adresses e-mail confondues, contre les essais en série.
    private final RateLimiter failedLoginsByAddress = new RateLimiter(20, Duration.ofMinutes(15));
    private final String dummyHash;

    public AdminAuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtUtils jwtUtils, AdminActivityService activities) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.dummyHash = passwordEncoder.encode("gmt-dummy-password");
        this.jwtUtils = jwtUtils;
        this.activities = activities;
    }

    public LoginResponse login(LoginRequest request) {
        return login(request, "inconnue");
    }

    public LoginResponse login(LoginRequest request, String clientAddress) {
        if (request == null || request.email() == null || request.motDePasse() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "E-mail et mot de passe requis");
        }
        String address = clientAddress == null || clientAddress.isBlank() ? "inconnue" : clientAddress;
        String attemptKey = request.email().trim().toLowerCase(Locale.ROOT) + "|" + address;
        if (failedLogins.isBlocked(attemptKey) || failedLoginsByAddress.isBlocked(address)) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "Trop de tentatives de connexion. Réessayez dans quelques minutes.");
        }
        User user = users.findByEmail(request.email().trim()).orElse(null);
        if (user == null) {
            // Même coût de calcul qu'un vrai compte, pour ne pas révéler quelles adresses existent.
            passwordEncoder.matches(request.motDePasse(), dummyHash);
            recordFailure(attemptKey, address);
            activities.record(request.email().trim(), "", "LOGIN_FAILED", "security", "Tentative de connexion refusée", false);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        boolean admin = role.equals("ADMIN") || role.equals("ROLE_ADMIN");
        if (!admin || user.getMotDePasse() == null || !passwordEncoder.matches(request.motDePasse(), user.getMotDePasse())) {
            recordFailure(attemptKey, address);
            activities.record(user.getEmail(), "", "LOGIN_FAILED", "security", "Tentative de connexion refusée", false);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Identifiants invalides");
        }
        if (Boolean.FALSE.equals(user.getEmailVerified())) {
            activities.record(user.getEmail(), "", "LOGIN_FAILED", "security", "Connexion refusée : adresse e-mail non vérifiée", false);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Veuillez vérifier votre adresse email avant de vous connecter.");
        }
        if (Boolean.FALSE.equals(user.getActif())) {
            activities.record(user.getEmail(), "", "LOGIN_FAILED", "security", "Connexion refusée : compte désactivé", false);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Ce compte est désactivé.");
        }
        failedLogins.reset(attemptKey);
        user.setLastLoginAt(LocalDateTime.now());
        users.save(user);
        activities.record(user.getEmail(), "", "LOGIN", "security", "Connexion administrateur réussie", true);
        return new LoginResponse(jwtUtils.generateToken(user.getEmail()), "Bearer", user.getId(), user.getEmail(), user.getNom(), user.getPrenom(), "ROLE_ADMIN");
    }

    private void recordFailure(String attemptKey, String address) {
        failedLogins.tryAcquire(attemptKey);
        failedLoginsByAddress.tryAcquire(address);
    }
}
