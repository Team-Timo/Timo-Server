package com.Timo.Timo.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Language {
	KO("Asia/Seoul"),
	EN("UTC");

	/**
	 * 클라이언트가 실제 시간대를 보내주기 전까지 사용할 언어별 기본 시간대(IANA tz).
	 * 언어와 시간대는 1:1이 아니므로 어디까지나 "초기 기본값"으로만 사용한다.
	 */
	private final String defaultZoneId;
}
