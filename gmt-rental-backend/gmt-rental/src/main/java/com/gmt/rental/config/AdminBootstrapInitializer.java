package com.gmt.rental.config;

import com.gmt.rental.entity.User;
import com.gmt.rental.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crée ou répare le compte administrateur configuré par GMT_ADMIN_EMAIL / GMT_ADMIN_PASSWORD.
 *
 * <ul>
 *   <li>Sans e-mail : aucune action.</li>
 *   <li>E-mail sans mot de passe, ou mot de passe de moins de 8 caractères : le démarrage échoue.</li>
 *   <li>Compte absent : créé en ADMIN avec le mot de passe fourni (haché).</li>
 *   <li>Compte existant déjà ADMIN : e-mail marqué vérifié ; mot de passe non modifié ; un compte
 *       désactivé reste désactivé (avertissement).</li>
 *   <li>Compte existant NON admin : promu ADMIN <strong>uniquement si son mot de passe correspond à
 *       GMT_ADMIN_PASSWORD</strong>. Sinon le démarrage échoue : un visiteur ayant créé ce compte avec
 *       l'e-mail de l'administrateur ne doit jamais devenir administrateur.</li>
 * </ul>
 */
@Component
public class AdminBootstrapInitializer implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapInitializer.class);

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrapInitializer(
            UserRepository users,
            PasswordEncoder passwordEncoder,
            @Value("${gmt.admin.email:}") String email,
            @Value("${gmt.admin.password:}") String password
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim();
        this.password = password == null ? "" : password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (email.isEmpty()) return;
        if (password.isBlank()) {
            throw new IllegalStateException("GMT_ADMIN_PASSWORD est obligatoire lorsque GMT_ADMIN_EMAIL est défini.");
        }
        if (password.length() < 8) {
            throw new IllegalStateException("GMT_ADMIN_PASSWORD doit contenir au moins 8 caractères.");
        }
        var existing = users.findByEmail(email);
        if (existing.isPresent()) {
            repairExistingAdmin(existing.get());
            return;
        }
        User admin = new User();
        admin.setEmail(email);
        admin.setNom("Administrateur");
        admin.setRole("ADMIN");
        admin.setActif(true);
        admin.setEmailVerified(true);
        admin.setMotDePasse(passwordEncoder.encode(password));
        users.save(admin);
        log.info("Compte administrateur créé à partir de GMT_ADMIN_EMAIL.");
    }

    private void repairExistingAdmin(User account) {
        boolean changed = false;
        if (!isAdminRole(account.getRole())) {
            if (!passwordEncoder.matches(password, account.getMotDePasse())) {
                throw new IllegalStateException("Un compte existe déjà avec l'e-mail GMT_ADMIN_EMAIL, avec un autre mot de passe : "
                        + "il n'est pas promu administrateur. Corrigez ce compte manuellement avant de redémarrer.");
            }
            account.setRole("ADMIN");
            changed = true;
            log.warn("Le compte configuré par GMT_ADMIN_EMAIL a été promu ADMIN (mot de passe vérifié).");
        }
        if (!Boolean.TRUE.equals(account.getEmailVerified())) {
            account.setEmailVerified(true);
            changed = true;
        }
        if (!Boolean.TRUE.equals(account.getActif())) {
            log.warn("Le compte configuré par GMT_ADMIN_EMAIL est désactivé : il reste désactivé jusqu'à réactivation manuelle.");
        }
        if (changed) users.save(account);
    }

    private static boolean isAdminRole(String role) {
        if (role == null) return false;
        String normalized = role.toUpperCase();
        return "ADMIN".equals(normalized) || "ROLE_ADMIN".equals(normalized);
    }
}
