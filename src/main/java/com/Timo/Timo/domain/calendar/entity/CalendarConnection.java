package com.Timo.Timo.domain.calendar.entity;

import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.global.crypto.AesGcmConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
  @JoinColumn(
      name = "user_id",
      nullable = false,
      unique = true,
      foreignKey = @ForeignKey(
          name = "fk_calendar_connections_user",
          foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
      )
  )
  private User user;

  @Column(name = "calendar_email", nullable = false)
  private String calendarEmail;

  @Convert(converter = AesGcmConverter.class)
  @Column(name = "access_token", nullable = false, length = 2048)
  private String accessToken;

  @Convert(converter = AesGcmConverter.class)
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

  public void updateAccessToken(String accessToken, LocalDateTime tokenExpiresAt) {
    this.accessToken = accessToken;
    this.tokenExpiresAt = tokenExpiresAt;
  }

  public String getTokenToRevoke() {
    return refreshToken != null ? refreshToken : accessToken;
  }
}
