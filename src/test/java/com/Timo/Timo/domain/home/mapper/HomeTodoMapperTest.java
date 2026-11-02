package com.Timo.Timo.domain.home.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.Timo.Timo.domain.home.dto.response.HomeResponse.TodoResponse;
import com.Timo.Timo.domain.home.mapper.HomeTodoMapper.TodoContext;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.enums.CalendarSource;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.vo.SubtaskStates;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;

class HomeTodoMapperTest {

	private static final LocalDate DATE = LocalDate.of(2026, 10, 15);

	private final HomeTodoMapper mapper = new HomeTodoMapper();
	private final User user = User.builder()
			.provider(Provider.GOOGLE)
			.providerId("provider-id")
			.name("티모")
			.email("timo@gmail.com")
			.build();

	@Test
	@DisplayName("구글 일정 투두는 공통 필드와 기존 구글 필드를 함께 채운다")
	void googleEventTodo() {
		Todo todo = Todo.createFromCalendarEvent(user, CalendarSource.GOOGLE, "구글 회의", DATE, "evt1_2026-10-15");
		todo.markDeletedFromCalendar();

		TodoResponse response = map(todo);

		assertThat(response.isCalendarEvent()).isTrue();
		assertThat(response.calendarSource()).isEqualTo(CalendarSource.GOOGLE);
		assertThat(response.isDeletedFromCalendar()).isTrue();
		assertThat(response.isDisconnectedFromCalendar()).isFalse();
		assertThat(response.isGoogleEvent()).isTrue();
		assertThat(response.isDeletedFromGoogle()).isTrue();
		assertThat(response.isDisconnectedFromGoogle()).isFalse();
	}

	@Test
	@DisplayName("애플 일정 투두는 공통 필드만 채우고 기존 구글 필드는 false로 둔다")
	void appleEventTodo() {
		Todo todo = Todo.createFromCalendarEvent(user, CalendarSource.APPLE, "애플 약속", DATE, "apple1_2026-10-15");
		todo.markDisconnectedFromCalendar();

		TodoResponse response = map(todo);

		assertThat(response.isCalendarEvent()).isTrue();
		assertThat(response.calendarSource()).isEqualTo(CalendarSource.APPLE);
		assertThat(response.isDisconnectedFromCalendar()).isTrue();
		assertThat(response.isGoogleEvent()).isFalse();
		assertThat(response.isDisconnectedFromGoogle()).isFalse();
	}

	@Test
	@DisplayName("티모에서 만든 투두는 캘린더 필드가 모두 비어 있다")
	void normalTodo() {
		Todo todo = Todo.create(user, null, "일반 투두", null, DATE, DATE, RepeatType.NONE,
				null, null, 0, null, null, null);

		TodoResponse response = map(todo);

		assertThat(response.isCalendarEvent()).isFalse();
		assertThat(response.calendarSource()).isNull();
		assertThat(response.isGoogleEvent()).isFalse();
	}

	private TodoResponse map(Todo todo) {
		return mapper.toResponse(new TodoContext(todo, null, null, 0, SubtaskStates.EMPTY));
	}
}
