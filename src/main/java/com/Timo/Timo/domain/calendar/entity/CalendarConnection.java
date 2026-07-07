package com.Timo.Timo.domain.calendar.entity;

import com.Timo.Timo.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "calendar_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CalendarConnection {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @OneToOne
  @Column(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "calendar_email", nullable = false)
  private String calendarEmail;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "access_token", nullable = false, length = 2048)
  private String accessToken;

  @Convert(converter = EncryptedStringConverter.class)
  @Column(name = "refresh_token", length = 2048)
  private String refreshToken;

  @Column(name = "token_expires_at")
  private LocalDateTime tokenExpiresAt;

  @Column(name = "connected_at", nullable = false)
  private LocalDateTime connectedAt;

  @Builder
  private CalendarConnection(
      User user,
      String calendarEmail,
      String accessToken,
      String refreshToken,
      LocalDateTime tokenExpiresAt
  ) {
    this.user = user;
    this.calendarEmail = calendarEmail;
    this.accessToken = accessToken;
    this.refreshToken = refreshToken;
    this.tokenExpiresAt = tokenExpiresAt;
    this.connectedAt = LocalDateTime.now();
  }
}
