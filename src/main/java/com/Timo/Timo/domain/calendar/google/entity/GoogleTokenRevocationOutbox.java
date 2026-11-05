package com.Timo.Timo.domain.calendar.google.entity;

import com.Timo.Timo.domain.calendar.google.enums.RevocationStatus;
import com.Timo.Timo.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calendar_revocation_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoogleTokenRevocationOutbox extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "token", nullable = false, length = 2048)
  private String token;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private RevocationStatus status;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  @Builder
  private GoogleTokenRevocationOutbox(String token) {
    this.token = token;
    this.status = RevocationStatus.PENDING;
    this.retryCount = 0;
  }

  public void markCompleted() {
    this.status = RevocationStatus.COMPLETED;
  }

  public void markFailed() {
    this.retryCount += 1;
    if (this.retryCount >= 5) {
      this.status = RevocationStatus.FAILED;
    }
  }
}
