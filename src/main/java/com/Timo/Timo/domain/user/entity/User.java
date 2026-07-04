package com.Timo.Timo.domain.user.entity;

import com.Timo.Timo.domain.user.enums.Provider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
public class User {

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

  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  @Builder
  private User(String email, String name, String profileImageUrl,
      Provider provider, String providerId) {
    this.email = email;
    this.name = name;
    this.profileImageUrl = profileImageUrl;
    this.provider = provider;
    this.providerId = providerId;
    this.language = "ko";
    this.wakeUpTime = LocalTime.of(7, 0);
    this.bedTime = LocalTime.of(23, 0);
    this.predictionAccuracy = 0L;
    this.onboardingCompleted = false;
    this.createdAt = LocalDateTime.now();
    this.updatedAt = LocalDateTime.now();
  }

  public void update(String name, String profileImageUrl) {
    this.name = name;
    this.profileImageUrl = profileImageUrl;
    this.updatedAt = LocalDateTime.now();
  }

  public void completeOnboarding() {
    this.onboardingCompleted = true;
    this.updatedAt = LocalDateTime.now();
  }
}
