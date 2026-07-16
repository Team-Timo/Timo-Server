package com.Timo.Timo.domain.timer.entity;

import com.Timo.Timo.domain.timer.exception.TimerErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "timer_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TimerSession {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "timer_record_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private TimerRecord timerRecord;

  @Column(name = "started_at", nullable = false)
  private LocalDateTime startedAt;

  @Column(name = "paused_at")
  private LocalDateTime pausedAt;

  @Builder
  private TimerSession(TimerRecord timerRecord, LocalDateTime startedAt){
    this.timerRecord = timerRecord;
    this.startedAt = startedAt;
  }

  public void pause(LocalDateTime pausedAt){
    if (!isActive()) {
      throw new CustomException(TimerErrorCode.TIMER_INVALID_STATUS_TRANSITION);
    }

    this.pausedAt = pausedAt;
  }

  public boolean isActive(){
    return this.pausedAt == null;
  }
}
