package com.gmt.rental.controller;

import com.gmt.rental.dto.admin.LoginRequest;
import com.gmt.rental.dto.admin.LoginResponse;
import com.gmt.rental.security.AuthCookies;
import com.gmt.rental.service.admin.AdminAuthService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AdminAuthController {
    private final AdminAuthService service;
    private final AuthCookies cookies;
    public AdminAuthController(AdminAuthService service, AuthCookies cookies) { this.service = service; this.cookies = cookies; }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest http) {
        LoginResponse result = service.login(request, http.getRemoteAddr());
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookies.issue(result.token())).body(result);
    }
}
