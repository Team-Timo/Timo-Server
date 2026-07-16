package com.Timo.Timo.domain.todo.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.Timo.Timo.domain.todo.enums.RepeatType;
import com.Timo.Timo.domain.todo.enums.TodoIcon;
import com.Timo.Timo.domain.todo.enums.TodoPriority;
import com.Timo.Timo.domain.todo.enums.Weekday;
import com.Timo.Timo.domain.todo.exception.TodoErrorCode;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.common.BaseTimeEntity;
import com.Timo.Timo.global.exception.CustomException;

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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "icon", length = 20)
	private TodoIcon icon;

	@Column(name = "title", nullable = false, length = 30)
	private String title;

	@Getter(AccessLevel.NONE)
	@OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Subtask> subtasks = new ArrayList<>();

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "repeat_type", nullable = false, length = 20)
	private RepeatType repeatType;

	@Getter(AccessLevel.NONE)
	@ElementCollection
	@CollectionTable(name = "todo_repeat_weekdays", joinColumns = @JoinColumn(name = "todo_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "weekday", nullable = false, length = 3)
	private List<Weekday> repeatWeekdays = new ArrayList<>();

	@Column(name = "repeat_day_of_month")
	private Integer repeatDayOfMonth;

	@Column(name = "duration_seconds", nullable = false)
	private Integer durationSeconds;

	@Enumerated(EnumType.STRING)
	@Column(name = "priority", length = 20)
	private TodoPriority priority;

	@Column(name = "tag_id")
	private Long tagId;

	@Lob
	@Column(name = "memo")
	private String memo;

	@Builder(access = AccessLevel.PRIVATE)
	private Todo(
			User user,
			TodoIcon icon,
			String title,
			LocalDate startDate,
			LocalDate endDate,
			RepeatType repeatType,
			List<Weekday> repeatWeekdays,
			Integer repeatDayOfMonth,
			Integer durationSeconds,
			TodoPriority priority,
			Long tagId,
			String memo
	) {
		this.user = user;
		this.icon = icon == TodoIcon.NONE ? null : icon;
		this.title = title;
		this.startDate = startDate;
		this.endDate = endDate;
		this.repeatType = repeatType;
		this.repeatWeekdays = repeatType == RepeatType.WEEKLY && repeatWeekdays != null
				? new ArrayList<>(repeatWeekdays) : new ArrayList<>();
		this.repeatDayOfMonth = repeatType == RepeatType.MONTHLY ? repeatDayOfMonth : null;
		this.durationSeconds = durationSeconds;
		this.priority = priority;
		this.tagId = tagId;
		this.memo = memo;
	}

	public static Todo create(
			User user,
			TodoIcon icon,
			String title,
			List<String> subtaskContents,
			LocalDate startDate,
			LocalDate endDate,
			RepeatType repeatType,
			List<Weekday> repeatWeekdays,
			Integer repeatDayOfMonth,
			int durationSeconds,
			TodoPriority priority,
			Long tagId,
			String memo
	) {
		Todo todo = Todo.builder()
				.user(user)
				.icon(icon)
				.title(title)
				.startDate(startDate)
				.endDate(endDate)
				.repeatType(repeatType)
				.repeatWeekdays(repeatWeekdays)
				.repeatDayOfMonth(repeatDayOfMonth)
				.durationSeconds(durationSeconds)
				.priority(priority)
				.tagId(tagId)
				.memo(memo)
				.build();

		if (subtaskContents != null) {
			for (int i = 0; i < subtaskContents.size(); i++) {
				todo.addSubtask(Subtask.of(subtaskContents.get(i), i + 1));
			}
		}
		return todo;
	}

	public void updateFields(
			TodoIcon icon,
			String title,
			Integer durationSeconds,
			TodoPriority priority,
			Long tagId,
			String memo
	) {
		if (icon == TodoIcon.NONE) {
			this.icon = null;
		} else if (icon != null) {
			this.icon = icon;
		}
		if (title != null) {
			this.title = title;
		}
		if (durationSeconds != null) {
			this.durationSeconds = durationSeconds;
		}
		if (priority != null) {
			this.priority = priority;
		}
		if (tagId != null) {
			this.tagId = tagId;
		}
		if (memo != null) {
			this.memo = memo;
		}
	}

	public void changeSchedule(
			LocalDate startDate,
			LocalDate endDate,
			RepeatType repeatType,
			List<Weekday> repeatWeekdays,
			Integer repeatDayOfMonth
	) {
		if (this.repeatType == RepeatType.DAILY && repeatType == RepeatType.DAILY
				&& !startDate.equals(this.startDate)) {
			throw new CustomException(TodoErrorCode.DAILY_DATE_CHANGE_NOT_ALLOWED);
		}

		this.startDate = startDate;
		this.endDate = endDate;
		this.repeatType = repeatType;
		this.repeatWeekdays = repeatType == RepeatType.WEEKLY && repeatWeekdays != null
				? new ArrayList<>(repeatWeekdays) : new ArrayList<>();
		this.repeatDayOfMonth = repeatType == RepeatType.MONTHLY ? repeatDayOfMonth : null;
	}

	public void replaceSubtasks(List<SubtaskEdit> edits) {
		Map<Long, Subtask> existingById = subtasks.stream()
				.filter(subtask -> subtask.getId() != null)
				.collect(Collectors.toMap(Subtask::getId, subtask -> subtask));

		List<Subtask> retained = new ArrayList<>();
		for (int i = 0; i < edits.size(); i++) {
			SubtaskEdit edit = edits.get(i);
			int sortOrder = i + 1;

			if (edit.subtaskId() != null) {
				Subtask existing = existingById.get(edit.subtaskId());
				if (existing == null) {
					throw new CustomException(TodoErrorCode.SUBTASK_NOT_FOUND);
				}
				existing.update(edit.content(), edit.completed(), sortOrder);
				retained.add(existing);
			} else {
				Subtask created = Subtask.of(edit.content(), sortOrder, edit.completed());
				created.assignTodo(this);
				retained.add(created);
			}
		}

		subtasks.removeIf(subtask -> !retained.contains(subtask));
		for (Subtask subtask : retained) {
			if (!subtasks.contains(subtask)) {
				subtasks.add(subtask);
			}
		}
	}

	public record SubtaskEdit(Long subtaskId, String content, boolean completed) { }

	public List<Subtask> getSubtasks() {
		if (subtasks == null) {
			return List.of();
		}
		return Collections.unmodifiableList(subtasks);
	}

	public List<Weekday> getRepeatWeekdays() {
		return Collections.unmodifiableList(repeatWeekdays);
	}

	private void addSubtask(Subtask subtask) {
		this.subtasks.add(subtask);
		subtask.assignTodo(this);
	}
}
