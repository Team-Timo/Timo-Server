package com.Timo.Timo.domain.user.service;

import com.Timo.Timo.domain.user.dto.request.OnboardingRequest;
import com.Timo.Timo.domain.user.dto.response.OnboardingResponse;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

  private static final String MIDNIGHT_24_FORMAT = "24:00";

  private final UserRepository userRepository;

  @Transactional
  public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request){

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    LocalTime wakeUpTime = LocalTime.parse(request.wakeUpTime());
    LocalTime bedTime = LocalTime.parse(request.bedTime());

    if (wakeUpTime.equals(bedTime)) {
      throw new CustomException(UserErrorCode.USER_INVALID_TIME);
    }

    user.completeOnboarding(
        request.language(),
        request.predictionAccuracy(),
        wakeUpTime,
        bedTime
    );

    return OnboardingResponse.builder()
        .onboardingCompleted(true)
        .termsAgreed(user.isTermsAgreed())
        .build();
  }

  private LocalTime parseTime(String timeValue) {
    if (MIDNIGHT_24_FORMAT.equals(timeValue)) {
      return LocalTime.MIDNIGHT;
    }
    try {
      return LocalTime.parse(timeValue);
    } catch (DateTimeParseException exception) {
      throw new CustomException(UserErrorCode.USER_INVALID_TIME);
    }
  }
}
