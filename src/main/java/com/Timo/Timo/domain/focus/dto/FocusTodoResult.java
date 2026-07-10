package com.Timo.Timo.domain.focus.dto;

import com.Timo.Timo.domain.focus.dto.response.FocusTodoResponse;
import com.Timo.Timo.domain.focus.exception.FocusSuccessCode;

public record FocusTodoResult(
		FocusSuccessCode successCode,
		FocusTodoResponse response
) { }
