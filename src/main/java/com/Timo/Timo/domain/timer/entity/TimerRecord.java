package com.Timo.Timo.domain.timer.entity;

import com.Timo.Timo.domain.timer.enums.TimerStatus;
import com.Timo.Timo.domain.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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

  @Column(name = "planned_minutes", nullable = false)
  private Integer plannedMinutes;

  @Column(name = "extended_minutes", nullable = false)
  private Integer extendedMinutes;

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

  @OneToMany(mappedBy = "timerRecord", cascade = CascadeType.ALL, orphanRemoval = true)
  private final List<TimerSession> sessions = new ArrayList<>();

  @Builder
  private TimerRecord(User user, Todo todo, Integer plannedMinutes, LocalDateTime startedAt) {
    this.user = user;
    this.todo = todo;
    this.plannedMinutes = plannedMinutes;
    this.startedAt = startedAt;
    this.extendedMinutes = 0;
    this.status = TimerStatus.RUNNING;
  }

  public void pause(){
    this.status = TimerStatus.PAUSED;
  }

  public void resume() {
    this.status = TimerStatus.RUNNING;
  }

  public void addSession(TimerSession session) {
    this.sessions.add(session);
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
