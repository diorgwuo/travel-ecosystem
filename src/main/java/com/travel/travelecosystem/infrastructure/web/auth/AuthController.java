package com.travel.travelecosystem.infrastructure.web.auth;

import com.travel.travelecosystem.infrastructure.web.auth.dto.AuthResponse;
import com.travel.travelecosystem.infrastructure.web.auth.dto.LoginRequest;
import com.travel.travelecosystem.infrastructure.web.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Пользователи", description = "Регистрация и вход")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @SecurityRequirements()
    @Operation(summary = "Регистрация пользователя")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    @SecurityRequirements()
    @Operation(summary = "Вход в систему")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
