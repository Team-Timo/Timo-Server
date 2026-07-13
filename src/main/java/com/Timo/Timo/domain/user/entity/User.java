package com.Timo.Timo.domain.user.entity;

import com.Timo.Timo.domain.user.enums.Language;
import com.Timo.Timo.domain.user.enums.Provider;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.global.common.BaseTimeEntity;
import com.Timo.Timo.global.exception.CustomException;
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
  @Column(name = "id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "provider", nullable = false, length = 20)
  private Provider provider;

  @Column(name = "provider_id", nullable = false)
  private String providerId;

  @Column(name = "name", nullable = false, length = 50)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "profile_image_url", length = 500)
  private String profileImageUrl;

  @Enumerated(EnumType.STRING)
  @Column(name = "language", nullable = false, length = 2)
  private Language language;

  @Column(name = "zone_id", nullable = false, length = 64)
  private String zoneId;

  @Column(name = "wake_up_time", nullable = false)
  private LocalTime wakeUpTime;

  @Column(name = "bed_time", nullable = false)
  private LocalTime bedTime;

  @Column(name = "prediction_accuracy", nullable = false)
  private Long predictionAccuracy;

  @Column(name = "onboarding_completed", nullable = false)
  private boolean onboardingCompleted;

  @Column(name = "terms_agreed", nullable = false)
  private boolean termsAgreed;

  public void update(String name, String profileImageUrl) {
    this.name = name;
    this.profileImageUrl = profileImageUrl;
  }

  public void completeOnboarding(
      Language language,
      Long predictionAccuracy,
      LocalTime wakeUpTime,
      LocalTime bedTime
  ) {
    this.language = language;
    this.predictionAccuracy = predictionAccuracy;
    this.wakeUpTime = wakeUpTime;
    this.bedTime = bedTime;
    this.termsAgreed = true;
    this.onboardingCompleted = true;
  }

  public void updateLanguage(Language language) {
    this.language = language;
  }

  public void updateZoneId(String zoneId) {
    this.zoneId = zoneId;
  }

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

    this.language = Language.KO;
    this.zoneId = Language.KO.getDefaultZoneId();
    this.wakeUpTime = LocalTime.of(7, 0);
    this.bedTime = LocalTime.of(23, 0);
    this.predictionAccuracy = 0L;
    this.onboardingCompleted = false;
    this.termsAgreed = false;
  }
}
