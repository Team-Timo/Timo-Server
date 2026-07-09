package com.Timo.Timo.domain.timer.controller;

import com.Timo.Timo.domain.timer.docs.TimerExtendControllerDocs;
import com.Timo.Timo.domain.timer.docs.TimerStartControllerDocs;
import com.Timo.Timo.domain.timer.docs.TimerStatusControllerDocs;
import com.Timo.Timo.domain.timer.dto.request.TimerActionRequest;
import com.Timo.Timo.domain.timer.dto.request.TimerExtendRequest;
import com.Timo.Timo.domain.timer.dto.response.TimerExtendResponse;
import com.Timo.Timo.domain.timer.dto.response.TimerStartResponse;
import com.Timo.Timo.domain.timer.dto.response.TimerStatusResponse;
import com.Timo.Timo.domain.timer.exception.TimerSuccessCode;
import com.Timo.Timo.domain.timer.service.TimerService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Timer", description = "타이머 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TimerController implements TimerStartControllerDocs, TimerStatusControllerDocs,
    TimerExtendControllerDocs  {

  private final TimerService timerService;

  @Override
  @PostMapping("/todos/{todoId}/timers/start")
  public ResponseEntity<BaseResponse<TimerStartResponse>> startTimer(
      @PathVariable Long todoId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {

    Long userId = userDetails.getUserId();
    TimerStartResponse response = timerService.startTimer(userId, todoId);

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.onSuccess(TimerSuccessCode.TIMER_STARTED, response));
  }

  @Override
  @PatchMapping("timers/{timerId}/status")
  public ResponseEntity<BaseResponse<TimerStatusResponse>> changeStatus(
      @PathVariable Long timerId,
      @Valid @RequestBody TimerActionRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    TimerStatusResponse response = timerService.changeStatus(userId, timerId, request.action());

    TimerSuccessCode successCode = switch (request.action()) {
      case PAUSE -> TimerSuccessCode.TIMER_PAUSED;
      case RESUME -> TimerSuccessCode.TIMER_RESUMED;
    };

    return ResponseEntity.ok()
        .body(BaseResponse.onSuccess(successCode, response));
  }

  @Override
  @PatchMapping("timers/{timerId}/extend")
  public ResponseEntity<BaseResponse<TimerExtendResponse>> extendTimer(
      @PathVariable Long timerId,
      @Valid @RequestBody TimerExtendRequest request,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    TimerExtendResponse response = timerService.extendTimer(userId, timerId, request.extendMinutes());

    return ResponseEntity.ok()
        .body(BaseResponse.onSuccess(TimerSuccessCode.TIMER_EXTENDED, response));
  }
}
