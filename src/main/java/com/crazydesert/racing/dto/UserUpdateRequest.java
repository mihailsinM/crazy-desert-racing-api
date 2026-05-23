package com.crazydesert.racing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class UserUpdateRequest {
    @NotBlank(message = "Name must not be blank")
    public String name;

    @Min(value = 1, message = "Age must be at least 1")
    public int age;

    @Email(message = "Email must be valid")
    @NotBlank(message = "Email must not be blank")
    public String email;

    public String licenseCategory;
    public boolean licenseVerified;
}
