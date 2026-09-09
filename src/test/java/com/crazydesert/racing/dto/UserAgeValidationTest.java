package com.crazydesert.racing.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAgeValidationTest {

    private final Validator validator = Validation
            .buildDefaultValidatorFactory()
            .getValidator();

    @Test
    void rejectsRegistrationForPersonUnderEighteen() {
        UserCreateRequest request = new UserCreateRequest();
        request.name = "Young Driver";
        request.age = 17;
        request.email = "young@example.com";
        request.licenseCategory = "B";
        request.password = "secure-password";

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void acceptsRegistrationAtEighteen() {
        UserCreateRequest request = new UserCreateRequest();
        request.name = "Adult Driver";
        request.age = 18;
        request.email = "adult@example.com";
        request.licenseCategory = "B";
        request.password = "secure-password";

        assertTrue(validator.validate(request).isEmpty());
    }
}
