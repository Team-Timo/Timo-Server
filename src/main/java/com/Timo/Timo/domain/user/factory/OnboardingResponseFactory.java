package com.Timo.Timo.domain.user.factory;

import com.Timo.Timo.domain.user.dto.response.OnboardingResponse;
import com.Timo.Timo.domain.user.exception.UserSuccessCode;
import com.Timo.Timo.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;

public class OnboardingResponseFactory {

  public static ResponseEntity<BaseResponse<OnboardingResponse>> completed(
      OnboardingResponse response
  ){
    return ResponseEntity.ok(
        BaseResponse.onSuccess(UserSuccessCode.ONBOARDING_COMPLETED, response)
    );
  }
}
