package com.app.sgelasmart.api.auth.dto;

public record UserResponse(
    Long id,
    String email,
    String name
) {}
