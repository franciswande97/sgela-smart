package com.app.sgelasmart.api.auth.dto;

public record LoginResponse(
    Long id,
    String email,
    String name,
    String token
) {}