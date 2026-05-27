package com.crazydesert.racing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthenticationRequest {

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    public String email;

    @NotBlank(message = "Password must not be blank")
    public String password;
}