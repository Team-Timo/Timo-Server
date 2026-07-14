package com.Timo.Timo.domain.terms.enums;

import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;

public enum TermsType {
	SERVICE,
	PRIVACY;

	public static TermsType from(String value) {
		if (value == null || value.isBlank()) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
		try {
			return TermsType.valueOf(value.trim());
		} catch (IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
	}
}