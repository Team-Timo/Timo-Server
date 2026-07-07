package com.Timo.Timo.domain.todo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TodoTitleValidator implements ConstraintValidator<ValidTodoTitle, String> {

    private static final int MAX_KOREAN_LENGTH = 20;
    private static final int MAX_ENGLISH_LENGTH = 30;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // @NotBlank가 별도로 처리
        }
        int maxLength = containsKorean(value) ? MAX_KOREAN_LENGTH : MAX_ENGLISH_LENGTH;
        return value.length() <= maxLength;
    }

    private boolean containsKorean(String value) {
        return value.chars().anyMatch(character -> character >= 0xAC00 && character <= 0xD7A3);
    }
}
