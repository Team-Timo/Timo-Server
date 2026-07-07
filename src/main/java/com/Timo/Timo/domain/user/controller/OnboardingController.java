package com.Timo.Timo.domain.user.controller;

import com.Timo.Timo.domain.user.docs.OnboardingControllerDocs;
import com.Timo.Timo.domain.user.dto.request.OnboardingRequest;
import com.Timo.Timo.domain.user.dto.response.OnboardingResponse;
import com.Timo.Timo.domain.user.factory.OnboardingResponseFactory;
import com.Timo.Timo.domain.user.service.OnboardingService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Onboarding", description = "온보딩 API")
public class OnboardingController implements OnboardingControllerDocs {

  private final OnboardingService onboardingService;

  @Override
  @PostMapping("/onboarding")
  public ResponseEntity<BaseResponse<OnboardingResponse>> completeOnboarding(
      @AuthenticationPrincipal CustomUserDetails userDetails,
      @Valid @RequestBody OnboardingRequest request
  ){
    Long userId = userDetails.getUserId();
    OnboardingResponse response = onboardingService.completeOnboarding(userId, request);

    return OnboardingResponseFactory.completed(response);
  }
}
