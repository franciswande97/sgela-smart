package com.app.sgelasmart.api.auth.dto;

public record LoginRequest(
    String email,
    String password
) {}
