package com.Timo.Timo.domain.user;

import com.Timo.Timo.domain.user.dto.request.OnboardingRequest;
import com.Timo.Timo.domain.user.dto.response.OnboardingResponse;
import com.Timo.Timo.domain.user.entity.User;
import com.Timo.Timo.domain.user.exception.UserErrorCode;
import com.Timo.Timo.domain.user.repository.UserRepository;
import com.Timo.Timo.global.exception.CustomException;
import java.time.LocalTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingService {

  private final UserRepository userRepository;

  @Transactional
  public OnboardingResponse completeOnboarding(Long userId, OnboardingRequest request){

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

    LocalTime wakeUpTime = LocalTime.parse(request.wakeUpTime());
    LocalTime bedTime = LocalTime.parse(request.bedTime());

    if (!bedTime.isAfter(wakeUpTime)) {
      throw new CustomException(UserErrorCode.USER_400_INVALID_TIME);
    }

    user.completeOnboarding(
        request.language(),
        request.predictionAccuracy(),
        wakeUpTime,
        bedTime
    );

    return OnboardingResponse.builder()
        .onboardingCompleted(true)
        .build();
  }
}
