package com.app.sgelasmart.api.auth.dto;

public record RegisterResponse(
    Long id,
    String email,
    String name,
    String token,
    String message
) {}