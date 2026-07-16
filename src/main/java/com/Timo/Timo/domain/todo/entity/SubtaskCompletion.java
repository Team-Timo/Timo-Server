package com.Timo.Timo.domain.todo.entity;

import com.Timo.Timo.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
		name = "subtask_completions",
		uniqueConstraints = @UniqueConstraint(
				name = "uk_subtask_completion",
				columnNames = {"todo_instance_id", "subtask_id"}
		)
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubtaskCompletion extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "todo_instance_id", nullable = false)
	private TodoInstance todoInstance;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "subtask_id", nullable = false)
	private Subtask subtask;

	@Column(name = "completed", nullable = false)
	private boolean completed;

	private SubtaskCompletion(TodoInstance todoInstance, Subtask subtask, boolean completed) {
		this.todoInstance = todoInstance;
		this.subtask = subtask;
		this.completed = completed;
	}

	public static SubtaskCompletion of(TodoInstance todoInstance, Subtask subtask, boolean completed) {
		return new SubtaskCompletion(todoInstance, subtask, completed);
	}

	public void updateCompleted(boolean completed) {
		this.completed = completed;
	}
}
