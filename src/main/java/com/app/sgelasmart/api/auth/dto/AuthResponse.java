package com.app.sgelasmart.api.auth.dto;

public record AuthResponse(
    Long id,
    String email,
    String name,
    String token,
    String message
) {}