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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subtasks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Subtask extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "todo_id", nullable = false)
	private Todo todo;

	@Column(name = "content", nullable = false, length = 20)
	private String content;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Column(name = "completed", nullable = false)
	private boolean completed;

	public static Subtask of(String content, int sortOrder) {
		Subtask subtask = new Subtask();
		subtask.content = content;
		subtask.sortOrder = sortOrder;
		subtask.completed = false;
		return subtask;
	}

	void assignTodo(Todo todo) {
		this.todo = todo;
	}

	public void updateCompleted(boolean completed) {
		this.completed = completed;
	}
}
