package com.Timo.Timo.domain.terms.enums;

import com.Timo.Timo.global.exception.CustomException;
import com.Timo.Timo.global.exception.code.ErrorCode;

public enum TermsLanguage {
	KO,
	EN;

	public static TermsLanguage from(String value) {
		try {
			return TermsLanguage.valueOf(value.trim());
		} catch (IllegalArgumentException exception) {
			throw new CustomException(ErrorCode.BAD_REQUEST);
		}
	}
}