package com.Timo.Timo.domain.todo.vo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.global.exception.CustomException;

public record Duration(int seconds) {

    private static final Pattern PATTERN = Pattern.compile("^(\\d+):([0-5]\\d)$");

    public static Duration parse(String value) {
        if (value == null || value.isBlank()) {
            throw new CustomException(TodoErrorCode.INVALID_REQUEST);
        }

        Matcher matcher = PATTERN.matcher(value);
        if (!matcher.matches()) {
            throw new CustomException(TodoErrorCode.INVALID_REQUEST);
        }

        int minutes = Integer.parseInt(matcher.group(1));
        int seconds = Integer.parseInt(matcher.group(2));
        return new Duration(minutes * 60 + seconds);
    }
}
