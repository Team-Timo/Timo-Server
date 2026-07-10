package com.Timo.Timo.domain.todo.enums;

import java.time.DayOfWeek;

public enum Weekday {
	MON,
	TUE,
	WED,
	THU,
	FRI,
	SAT,
	SUN;

	public static Weekday from(DayOfWeek dayOfWeek) {
		return switch (dayOfWeek) {
			case MONDAY -> MON;
			case TUESDAY -> TUE;
			case WEDNESDAY -> WED;
			case THURSDAY -> THU;
			case FRIDAY -> FRI;
			case SATURDAY -> SAT;
			case SUNDAY -> SUN;
		};
	}
}
