package com.gmt.rental.config;

import com.gmt.rental.entity.User;
import com.gmt.rental.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapInitializerTests {
    private final ApplicationArguments args = mock(ApplicationArguments.class);

    private AdminBootstrapInitializer initializer(UserRepository users, PasswordEncoder encoder, String email, String password) {
        return new AdminBootstrapInitializer(users, encoder, email, password);
    }

    @Test
    void doesNothingWhenNoEmailConfigured() throws Exception {
        UserRepository users = mock(UserRepository.class);
        initializer(users, mock(PasswordEncoder.class), "", "").run(args);
        verifyNoInteractions(users);
    }

    @Test
    void failsWhenEmailIsSetWithoutPassword() {
        UserRepository users = mock(UserRepository.class);
        AdminBootstrapInitializer init = initializer(users, mock(PasswordEncoder.class), "admin@example.invalid", "");
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> init.run(args));
        assertTrue(error.getMessage().contains("GMT_ADMIN_PASSWORD"));
        verifyNoInteractions(users);
    }

    @Test
    void failsWhenPasswordIsTooShort() {
        UserRepository users = mock(UserRepository.class);
        AdminBootstrapInitializer init = initializer(users, mock(PasswordEncoder.class), "admin@example.invalid", "short");
        assertThrows(IllegalStateException.class, () -> init.run(args));
        verifyNoInteractions(users);
    }

    @Test
    void createsAdminWithHashedPasswordWhenAccountIsAbsent() throws Exception {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(users.findByEmail("admin@example.invalid")).thenReturn(Optional.empty());
        when(encoder.encode("long-enough-password")).thenReturn("hashed");
        initializer(users, encoder, " admin@example.invalid ", "long-enough-password").run(args);
        verify(users).save(argThat(user -> "ADMIN".equals(user.getRole())
                && "hashed".equals(user.getMotDePasse())
                && Boolean.TRUE.equals(user.getActif())
                && Boolean.TRUE.equals(user.getEmailVerified())));
    }

    @Test
    void promotesExistingAccountOnlyWhenConfiguredPasswordMatches() throws Exception {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        User customer = new User();
        customer.setEmail("admin@example.invalid");
        customer.setRole("USER");
        customer.setActif(false);
        customer.setEmailVerified(false);
        customer.setMotDePasse("existing-hash");
        when(users.findByEmail("admin@example.invalid")).thenReturn(Optional.of(customer));
        when(encoder.matches("long-enough-password", "existing-hash")).thenReturn(true);
        initializer(users, encoder, "admin@example.invalid", "long-enough-password").run(args);
        assertEquals("ADMIN", customer.getRole());
        assertTrue(customer.getEmailVerified());
        assertFalse(customer.getActif(), "Un compte désactivé ne doit pas être réactivé au démarrage.");
        assertEquals("existing-hash", customer.getMotDePasse());
        verify(users).save(customer);
    }

    @Test
    void refusesToPromoteExistingAccountWithDifferentPassword() {
        UserRepository users = mock(UserRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        User attackerAccount = new User();
        attackerAccount.setEmail("admin@example.invalid");
        attackerAccount.setRole("USER");
        attackerAccount.setActif(true);
        attackerAccount.setEmailVerified(false);
        attackerAccount.setMotDePasse("attacker-hash");
        when(users.findByEmail("admin@example.invalid")).thenReturn(Optional.of(attackerAccount));
        when(encoder.matches("long-enough-password", "attacker-hash")).thenReturn(false);
        AdminBootstrapInitializer init = initializer(users, encoder, "admin@example.invalid", "long-enough-password");
        assertThrows(IllegalStateException.class, () -> init.run(args));
        assertEquals("USER", attackerAccount.getRole(), "Un compte au mot de passe différent ne doit jamais devenir ADMIN.");
        verify(users, never()).save(any());
    }

    @Test
    void leavesAlreadyCorrectAdminUntouched() throws Exception {
        UserRepository users = mock(UserRepository.class);
        User admin = new User();
        admin.setEmail("admin@example.invalid");
        admin.setRole("ADMIN");
        admin.setActif(true);
        admin.setEmailVerified(true);
        when(users.findByEmail("admin@example.invalid")).thenReturn(Optional.of(admin));
        initializer(users, mock(PasswordEncoder.class), "admin@example.invalid", "long-enough-password").run(args);
        verify(users, never()).save(any());
    }
}
