package ru.netology.cloudservise.other;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.netology.cloudservise.dto.LoginRequest;
import ru.netology.cloudservise.dto.RenameFileRequest;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void loginRequest_BlankFields_Violations() {
        LoginRequest request = new LoginRequest("", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
    }

    @Test
    void renameFileRequest_BlankName_Violation() {
        RenameFileRequest request = new RenameFileRequest("");

        Set<ConstraintViolation<RenameFileRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
    }
}