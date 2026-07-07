package com.Timo.Timo.domain.todo.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "todos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Todo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "todo_id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "tag_id")
	private Long tagId;

	@Column(name = "sorted_order", nullable = false)
	private Integer sortedOrder;

	@Column(name = "memo", length = 300)
	private String memo;

	@Column(name = "scheduled_date")
	private LocalDate scheduledDate;

	@Column(name = "priority", length = 10)
	private String priority;

	@Column(name = "duration_time")
	private Integer durationTime;

	@Column(name = "icon", length = 30)
	private String icon;

	@Column(name = "is_completed", nullable = false)
	private boolean completed;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;
}
