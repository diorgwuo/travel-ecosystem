package com.travel.travelecosystem.infrastructure.web.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter and one digit"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name must be up to 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name must be up to 100 characters")
    private String lastName;

    /** TRAVELER (турист) или OPERATOR (туроператор). Администратор через регистрацию недоступен. */
    @NotNull(message = "Role is required")
    private RegisterRole role;
}
