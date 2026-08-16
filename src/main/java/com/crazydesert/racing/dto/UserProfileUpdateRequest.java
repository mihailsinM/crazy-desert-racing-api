package com.crazydesert.racing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserProfileUpdateRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 80, message = "Name must be at most 80 characters")
    public String name;

    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be at most 120")
    public int age;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    @Size(max = 160, message = "Email must be at most 160 characters")
    public String email;

    @NotBlank(message = "License category must not be blank")
    @Size(max = 50, message = "License category must be at most 50 characters")
    public String licenseCategory;
}
