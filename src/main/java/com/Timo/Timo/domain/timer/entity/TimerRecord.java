package com.Timo.Timo.domain.timer.entity;

import com.Timo.Timo.domain.timer.enums.TimerStatus;
import com.Timo.Timo.domain.timer.exception.TimerErrorCode;
import com.Timo.Timo.domain.todo.entity.Todo;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "timer_records")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimerRecord {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "todo_id", nullable = false)
  private Todo todo;

  @Column(name = "planned_seconds", nullable = false)
  private Integer plannedSeconds;

  @Column(name = "extended_seconds", nullable = false)
  private Integer extendedSeconds;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "ended_at")
  private LocalDateTime endedAt;

  @Column(name = "actual_seconds")
  private Integer actualSeconds;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private TimerStatus status;

  @Column(name = "ai_feedback", length = 500)
  private String aiFeedback;

  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Builder
  private TimerRecord(User user, Todo todo, Integer plannedSeconds, LocalDateTime startedAt) {
    this.user = user;
    this.todo = todo;
    this.plannedSeconds = plannedSeconds;
    this.startedAt = startedAt;
    this.extendedSeconds = 0;
    this.status = TimerStatus.RUNNING;
  }

  public void pause() {
    if (!isRunning()) {
      throw new CustomException(TimerErrorCode.TIMER_INVALID_STATUS_TRANSITION);
    }
    this.status = TimerStatus.PAUSED;
  }

  public void resume() {
    if (!isPaused()) {
      throw new CustomException(TimerErrorCode.TIMER_INVALID_STATUS_TRANSITION);
    }
    this.status = TimerStatus.RUNNING;
  }

  public void extend(int extendedSeconds){
    if (isFinished()){
      throw new CustomException(TimerErrorCode.TIMER_ALREADY_FINISHED);
    }
    this.extendedSeconds += extendedSeconds;
  }

  public boolean isRunning() {
    return this.status == TimerStatus.RUNNING;
  }

  public boolean isPaused() {
    return this.status == TimerStatus.PAUSED;
  }

  public boolean isFinished() {
    return this.status == TimerStatus.COMPLETED || this.status == TimerStatus.STOPPED;
  }
}
