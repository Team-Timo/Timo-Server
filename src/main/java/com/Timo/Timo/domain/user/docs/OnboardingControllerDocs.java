package com.Timo.Timo.domain.user.docs;

import com.Timo.Timo.domain.user.dto.request.OnboardingRequest;
import com.Timo.Timo.domain.user.dto.response.OnboardingResponse;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface OnboardingControllerDocs {

  @Operation(summary = "온보딩 완료", description = "언어, 예측 정확도, 기상/취침 시간을 저장하고 온보딩을 완료 처리합니다.")
  ResponseEntity<BaseResponse<OnboardingResponse>> completeOnboarding(
      CustomUserDetails userDetails,
      OnboardingRequest request
  );
}
