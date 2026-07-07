package com.Timo.Timo.domain.todo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SubtaskContentValidator implements ConstraintValidator<ValidSubtaskContent, String> {

    private static final int MAX_KOREAN_LENGTH = 12;
    private static final int MAX_ENGLISH_LENGTH = 20;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int maxLength = containsKorean(value) ? MAX_KOREAN_LENGTH : MAX_ENGLISH_LENGTH;
        return value.length() <= maxLength;
    }

    private boolean containsKorean(String value) {
        return value.chars().anyMatch(character -> character >= 0xAC00 && character <= 0xD7A3);
    }
}
