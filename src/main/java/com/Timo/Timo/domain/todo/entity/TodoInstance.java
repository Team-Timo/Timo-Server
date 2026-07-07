package com.Timo.Timo.domain.todo.entity;

import java.time.LocalDate;

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
        name = "todo_instances",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_todo_instance",
                columnNames = {"todo_id", "instance_date"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TodoInstance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(name = "instance_date", nullable = false)
    private LocalDate date;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static TodoInstance of(Todo todo, LocalDate date, int sortOrder) {
        TodoInstance instance = new TodoInstance();
        instance.todo = todo;
        instance.date = date;
        instance.completed = false;
        instance.sortOrder = sortOrder;
        return instance;
    }

    public void markCompleted() {
        this.completed = true;
    }

    public void markIncomplete() {
        this.completed = false;
    }

    public void updateSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
