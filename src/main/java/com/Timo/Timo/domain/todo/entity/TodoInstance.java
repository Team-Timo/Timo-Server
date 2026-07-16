package com.Timo.Timo.domain.todo.entity;

import java.time.LocalDate;

import com.Timo.Timo.domain.todo.enums.TodoTimerStatus;
import com.Timo.Timo.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Todo todo;

    @Column(name = "instance_date", nullable = false)
    private LocalDate date;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "timer_status", nullable = false, length = 20)
    private TodoTimerStatus timerStatus = TodoTimerStatus.STOPPED;

    @Lob
    @Column(name = "memo")
    private String memo;

    public static TodoInstance of(Todo todo, LocalDate date, int sortOrder) {
        TodoInstance instance = new TodoInstance();
        instance.todo = todo;
        instance.date = date;
        instance.completed = false;
        instance.sortOrder = sortOrder;
        instance.timerStatus = TodoTimerStatus.STOPPED;
        return instance;
    }

    public TodoTimerStatus getTimerStatus() {
        return timerStatus != null ? timerStatus : TodoTimerStatus.STOPPED;
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

    public void startTimer() {
        this.timerStatus = TodoTimerStatus.RUNNING;
    }

    public void pauseTimer() {
        this.timerStatus = TodoTimerStatus.PAUSED;
    }

    public void stopTimer() {
        this.timerStatus = TodoTimerStatus.STOPPED;
    }

    public void updateMemo(String memo) {
        this.memo = memo;
    }

    public String resolveMemo() {
        return memo != null ? memo : todo.getMemo();
    }
}
