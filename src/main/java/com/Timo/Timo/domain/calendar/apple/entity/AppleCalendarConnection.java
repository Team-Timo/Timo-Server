package com.Timo.Timo.domain.calendar.apple.entity;

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

@Entity
@Table(name = "apple_calendar_connections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AppleCalendarConnection {

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
          name = "fk_apple_calendar_connections_user",
          foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
      )
  )
  private User user;

  @Column(name = "apple_id", nullable = false)
  private String appleId;

  @Convert(converter = AesGcmConverter.class)
  @Column(name = "app_password", nullable = false, length = 512)
  private String appPassword;

  @Column(name = "calendar_home_url", nullable = false, length = 1024)
  private String calendarHomeUrl;

  @Column(name = "connected_at", nullable = false)
  private LocalDateTime connectedAt;

  @Builder
  private AppleCalendarConnection(User user, String appleId, String appPassword, String calendarHomeUrl) {
    this.user = user;
    this.appleId = appleId;
    this.appPassword = appPassword;
    this.calendarHomeUrl = calendarHomeUrl;
    this.connectedAt = LocalDateTime.now();
  }

  public void updateCalendarHomeUrl(String calendarHomeUrl) {
    this.calendarHomeUrl = calendarHomeUrl;
  }
}
