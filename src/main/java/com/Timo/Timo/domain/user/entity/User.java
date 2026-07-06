package com.Timo.Timo.domain.user.entity;

import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"provider", "provider_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Provider provider;

  @Column(name = "provider_id", nullable = false)
  private String providerId;

  @Column(nullable = false, length = 50)
  private String name;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  @Column(nullable = false, length = 5)
  private String language;

  @Column(name = "wake_up_time", nullable = false)
  private LocalTime wakeUpTime;

  @Column(name = "bed_time", nullable = false)
  private LocalTime bedTime;

  @Column(name = "prediction_accuracy", nullable = false)
  private Long predictionAccuracy;

  @Column(name = "onboarding_completed", nullable = false)
  private boolean onboardingCompleted;

  @Column(name = "calendar_connected", nullable = false)
  private boolean calendarConnected;

  @Column(name = "calendar_email")
  private String calendarEmail;

  @Builder
  private User(
      Provider provider,
      String providerId,
      String name,
      String email,
      String profileImageUrl
  ) {
    this.provider = provider;
    this.providerId = providerId;
    this.name = name;
    this.email = email;
    this.profileImageUrl = profileImageUrl;

    this.language = "ko";
    this.wakeUpTime = LocalTime.of(7, 0);
    this.bedTime = LocalTime.of(23, 0);
    this.predictionAccuracy = 0L;
    this.onboardingCompleted = false;

    this.calendarConnected = false;
    this.calendarEmail = null;
  }

  public void update(String name, String profileImageUrl) {
    this.name = name;
    this.profileImageUrl = profileImageUrl;
  }

  public void completeOnboarding() {
    this.onboardingCompleted = true;
  }
}
