package com.Timo.Timo.domain.todo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoIcon;
import com.Timo.Timo.domain.todo.enums.TodoPriority;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.common.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "icon", length = 20)
	private TodoIcon icon;

	@Column(name = "title", nullable = false, length = 30)
	private String title;

	@OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Subtask> subtasks = new ArrayList<>();

	@Column(name = "todo_date", nullable = false)
	private LocalDate date;

	@Column(name = "duration_seconds", nullable = false)
	private Integer durationSeconds;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", length = 20)
	private TodoPriority priority;

	@Column(name = "tag_id")
	private Long tagId;

	@Enumerated(EnumType.STRING)
	@Column(name = "repeat_type", nullable = false, length = 20)
	private RepeatType repeatType;

	@ElementCollection
	@CollectionTable(name = "todo_repeat_weekdays", joinColumns = @JoinColumn(name = "todo_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "weekday", nullable = false, length = 3)
	private List<Weekday> repeatWeekdays = new ArrayList<>();

	@Column(name = "repeat_day_of_month")
	private Integer repeatDayOfMonth;

	@Lob
	@Column(name = "memo")
	private String memo;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Builder(access = AccessLevel.PRIVATE)
	private Todo(
			User user,
			TodoIcon icon,
			String title,
			LocalDate date,
			Integer durationSeconds,
			TodoPriority priority,
			Long tagId,
			RepeatType repeatType,
			List<Weekday> repeatWeekdays,
			Integer repeatDayOfMonth,
			String memo,
			Integer sortOrder
	) {
		this.user = user;
		this.icon = icon;
		this.title = title;
		this.date = date;
		this.durationSeconds = durationSeconds;
		this.priority = priority;
		this.tagId = tagId;
		this.repeatType = repeatType;
		this.repeatWeekdays = repeatWeekdays != null ? new ArrayList<>(repeatWeekdays) : new ArrayList<>();
		this.repeatDayOfMonth = repeatDayOfMonth;
		this.memo = memo;
		this.sortOrder = sortOrder;
	}

	public static Todo create(
			User user,
			TodoIcon icon,
			String title,
			List<String> subtaskContents,
			LocalDate date,
			int durationSeconds,
			TodoPriority priority,
			Long tagId,
			RepeatType repeatType,
			List<Weekday> repeatWeekdays,
			Integer repeatDayOfMonth,
			String memo,
			int sortOrder
	) {
		Todo todo = Todo.builder()
				.user(user)
				.icon(icon)
				.title(title)
				.date(date)
				.durationSeconds(durationSeconds)
				.priority(priority)
				.tagId(tagId)
				.repeatType(repeatType)
				.repeatWeekdays(repeatWeekdays)
				.repeatDayOfMonth(repeatDayOfMonth)
				.memo(memo)
				.sortOrder(sortOrder)
				.build();

		if (subtaskContents != null) {
			subtaskContents.forEach(content -> todo.addSubtask(Subtask.of(content)));
		}
		return todo;
	}

	private void addSubtask(Subtask subtask) {
		this.subtasks.add(subtask);
		subtask.assignTodo(this);
	}
}
