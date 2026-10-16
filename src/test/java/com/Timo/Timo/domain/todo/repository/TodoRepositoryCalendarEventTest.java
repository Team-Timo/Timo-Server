package com.Timo.Timo.domain.todo.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.enums.Provider;

@DataJpaTest(properties = "security.crypto.calendar-token-key=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class TodoRepositoryCalendarEventTest {

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0");

	@Autowired
	private TodoRepository todoRepository;

	@Autowired
	private TestEntityManager em;

	private User owner;
	private User otherUser;

	@BeforeEach
	void setUp() {
		owner = em.persist(user("owner@gmail.com"));
		otherUser = em.persist(user("other@gmail.com"));
	}

	@Test
	@DisplayName("기간 내 외부 일정 투두는 해당 사용자의 외부 일정 투두만 조회한다")
	void findGoogleEventsInRange() {
		Todo inRange = em.persist(googleTodo(owner, date(15), "evt1_2026-10-15"));
		em.persist(googleTodo(owner, date(25), "evt2_2026-10-25"));
		em.persist(normalTodo(owner, date(15)));
		em.persist(googleTodo(otherUser, date(15), "evt3_2026-10-15"));
		em.flush();
		em.clear();

		List<Todo> result = todoRepository.findGoogleEventsInRange(owner.getId(), date(10), date(20));

		assertThat(result).extracting(Todo::getId).containsExactly(inRange.getId());
	}

	@Test
	@DisplayName("기준일 이후 외부 일정 투두는 기준일 당일을 제외하고 조회한다")
	void findGoogleEventsAfter() {
		em.persist(googleTodo(owner, date(15), "evt1_2026-10-15"));
		Todo after = em.persist(googleTodo(owner, date(16), "evt2_2026-10-16"));
		em.persist(normalTodo(owner, date(16)));
		em.persist(googleTodo(otherUser, date(16), "evt3_2026-10-16"));
		em.flush();
		em.clear();

		List<Todo> result = todoRepository.findGoogleEventsAfter(owner.getId(), date(15));

		assertThat(result).extracting(Todo::getId).containsExactly(after.getId());
	}

	@Test
	@DisplayName("기준일까지의 외부 일정 투두 중 아직 표시되지 않은 투두만 연동 해제됨으로 표시한다")
	void markDisconnectedFromGoogleUpTo() {
		Todo past = em.persist(googleTodo(owner, date(14), "evt1_2026-10-14"));
		Todo today = em.persist(googleTodo(owner, date(15), "evt2_2026-10-15"));
		Todo alreadyMarked = googleTodo(owner, date(13), "evt3_2026-10-13");
		alreadyMarked.markDisconnectedFromGoogle();
		em.persist(alreadyMarked);
		Todo future = em.persist(googleTodo(owner, date(16), "evt4_2026-10-16"));
		Todo normal = em.persist(normalTodo(owner, date(14)));
		Todo otherUsers = em.persist(googleTodo(otherUser, date(14), "evt5_2026-10-14"));
		em.flush();
		em.clear();

		int updated = todoRepository.markDisconnectedFromGoogleUpTo(owner.getId(), date(15), LocalDateTime.now());
		em.clear();

		assertThat(updated).isEqualTo(2);
		assertThat(reload(past).isDisconnectedFromGoogle()).isTrue();
		assertThat(reload(today).isDisconnectedFromGoogle()).isTrue();
		assertThat(reload(future).isDisconnectedFromGoogle()).isFalse();
		assertThat(reload(normal).isDisconnectedFromGoogle()).isFalse();
		assertThat(reload(otherUsers).isDisconnectedFromGoogle()).isFalse();
	}

	@Test
	@DisplayName("재연동 시 해당 사용자의 연동 해제됨 표시만 일괄 해제한다")
	void clearDisconnectedFromGoogleByUserId() {
		Todo mine = googleTodo(owner, date(14), "evt1_2026-10-14");
		mine.markDisconnectedFromGoogle();
		em.persist(mine);
		Todo others = googleTodo(otherUser, date(14), "evt2_2026-10-14");
		others.markDisconnectedFromGoogle();
		em.persist(others);
		em.flush();
		em.clear();

		int updated = todoRepository.clearDisconnectedFromGoogleByUserId(owner.getId(), LocalDateTime.now());
		em.clear();

		assertThat(updated).isEqualTo(1);
		assertThat(reload(mine).isDisconnectedFromGoogle()).isFalse();
		assertThat(reload(others).isDisconnectedFromGoogle()).isTrue();
	}

	@Test
	@DisplayName("외부 일정 ID로 해당 사용자의 투두만 조회한다")
	void findByUserIdAndExternalEventIdIn() {
		Todo mine = em.persist(googleTodo(owner, date(15), "evt1_2026-10-15"));
		em.persist(googleTodo(otherUser, date(15), "evt1_2026-10-15"));
		em.flush();
		em.clear();

		List<Todo> result = todoRepository.findByUser_IdAndExternalEventIdIn(
				owner.getId(), List.of("evt1_2026-10-15", "unknown"));

		assertThat(result).extracting(Todo::getId).containsExactly(mine.getId());
	}

	@Test
	@DisplayName("같은 사용자에게 같은 외부 일정 ID의 투두는 중복 저장할 수 없다")
	void uniqueExternalEventIdPerUser() {
		em.persist(googleTodo(owner, date(15), "evt1_2026-10-15"));
		em.flush();

		assertThatThrownBy(() -> todoRepository.saveAndFlush(googleTodo(owner, date(15), "evt1_2026-10-15")))
				.isInstanceOf(DataIntegrityViolationException.class);
	}

	private Todo reload(Todo todo) {
		return em.find(Todo.class, todo.getId());
	}

	private User user(String email) {
		return User.builder()
				.provider(Provider.GOOGLE)
				.providerId(email)
				.name("티모")
				.email(email)
				.build();
	}

	private Todo googleTodo(User user, LocalDate date, String externalEventId) {
		return Todo.createFromGoogleEvent(user, "구글 일정", date, externalEventId);
	}

	private Todo normalTodo(User user, LocalDate date) {
		return Todo.create(user, null, "일반 투두", null, date, date, RepeatType.NONE,
				null, null, 0, null, null, null);
	}

	private LocalDate date(int day) {
		return LocalDate.of(2026, 10, day);
	}
}
