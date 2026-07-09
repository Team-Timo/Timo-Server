package com.Timo.Timo.domain.terms.enums;

import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;

public enum TermsType {
	SERVICE,
	PRIVACY;

	public static TermsType from(String value) {
		try {
			return TermsType.valueOf(value.trim());
		} catch (IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
	}
}