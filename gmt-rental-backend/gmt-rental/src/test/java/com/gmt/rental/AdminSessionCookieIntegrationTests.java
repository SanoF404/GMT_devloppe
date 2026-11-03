package com.gmt.rental;

import com.gmt.rental.entity.User;
import com.gmt.rental.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:gmt_rental_session_cookie_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.mail.host=localhost",
        "spring.mail.username=",
        "spring.mail.password=",
        "gmt.email.resend-api-key=",
        "gmt.email.from=",
        "gmt.support.email=",
        "gmt.jwt.secret=test-only-jwt-signing-key-long-enough-for-hmac"
})
@AutoConfigureMockMvc
class AdminSessionCookieIntegrationTests {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository users;
    @Autowired private PasswordEncoder passwords;

    @Test
    void sessionLivesInHttpOnlyCookieAndStateChangesRequireCsrfHeader() throws Exception {
        String email = "cookie-admin-" + UUID.randomUUID() + "@gmt.test";
        User admin = new User();
        admin.setNom("Administrateur"); admin.setPrenom("Cookie"); admin.setEmail(email);
        admin.setRole("ADMIN"); admin.setActif(true); admin.setEmailVerified(true);
        admin.setMotDePasse(passwords.encode("cookie-password"));
        users.save(admin);

        MvcResult login = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"motDePasse\":\"cookie-password\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").doesNotExist())
                .andReturn();

        String setCookie = login.getResponse().getHeader("Set-Cookie");
        assertNotNull(setCookie, "La connexion doit poser le cookie de session.");
        assertTrue(setCookie.startsWith("gmt_session="), "Le cookie doit s'appeler gmt_session.");
        assertTrue(setCookie.contains("HttpOnly"), "Le cookie de session doit être httpOnly.");
        String sessionValue = setCookie.substring("gmt_session=".length(), setCookie.indexOf(';'));
        assertFalse(sessionValue.isBlank());

        mockMvc.perform(get("/api/admin/account/profile").cookie(new Cookie("gmt_session", sessionValue)))
                .andExpect(status().isOk());

        // Requête modifiante authentifiée par cookie sans en-tête de protection : refusée.
        mockMvc.perform(post("/api/admin/account/logout").cookie(new Cookie("gmt_session", sessionValue)))
                .andExpect(status().isForbidden());

        // Avec l'en-tête de protection : acceptée, et le cookie est effacé.
        MvcResult logout = mockMvc.perform(post("/api/admin/account/logout")
                        .cookie(new Cookie("gmt_session", sessionValue))
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isNoContent())
                .andReturn();
        String cleared = logout.getResponse().getHeader("Set-Cookie");
        assertNotNull(cleared);
        assertTrue(cleared.contains("Max-Age=0"), "La déconnexion doit expirer le cookie.");
    }
}
