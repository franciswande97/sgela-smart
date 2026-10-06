package com.app.sgelasmart.api.auth;

import com.app.sgelasmart.api.auth.dto.AuthResponse;
import com.app.sgelasmart.api.auth.dto.LoginRequest;
import com.app.sgelasmart.api.auth.dto.RegisterRequest;
import com.app.sgelasmart.model.User;
import com.app.sgelasmart.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User user = new User(request.email(), hashedPassword, request.name());
        User savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser.getEmail());

        return new AuthResponse(
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getName(),
            token,
            "User registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(
            user.getId(),
            user.getEmail(),
            user.getName(),
            token,
            "Login successful"
        );
    }
}