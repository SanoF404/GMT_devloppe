package com.gmt.rental.dto.admin;

import com.fasterxml.jackson.annotation.JsonIgnore;

/** Le jeton n'est jamais renvoyé dans le corps : il part uniquement dans le cookie httpOnly. */
public record LoginResponse(@JsonIgnore String token, String type, Long id, String email, String nom, String prenom, String role) {}
