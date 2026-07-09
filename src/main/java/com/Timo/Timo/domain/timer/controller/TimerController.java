package com.Timo.Timo.domain.timer.controller;

import com.Timo.Timo.domain.timer.docs.TimerCompleteControllerDocs;
import com.Timo.Timo.domain.timer.docs.TimerControllerDocs;
import com.Timo.Timo.domain.timer.docs.TimerStopControllerDocs;
import com.Timo.Timo.domain.timer.dto.response.TimerFinishResponse;
import com.Timo.Timo.domain.timer.dto.response.TimerStartResponse;
import com.Timo.Timo.domain.timer.exception.TimerSuccessCode;
import com.Timo.Timo.domain.timer.factory.TimerResponseFactory;
import com.Timo.Timo.domain.timer.service.TimerService;
import com.Timo.Timo.global.auth.principal.CustomUserDetails;
import com.Timo.Timo.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Timer", description = "타이머 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TimerController implements TimerControllerDocs, TimerCompleteControllerDocs,
    TimerStopControllerDocs {

  private final TimerService timerService;
  private final TimerResponseFactory timerResponseFactory;

  @Override
  @PostMapping("/todos/{todoId}/timers/start")
  public ResponseEntity<BaseResponse<TimerStartResponse>> startTimer(
      @PathVariable Long todoId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {

    Long userId = userDetails.getUserId();
    TimerStartResponse response = timerService.startTimer(userId, todoId);

    return timerResponseFactory.startResponse(response);
  }

  @Override
  @PatchMapping("/timers/{timerId}/complete")
  public ResponseEntity<BaseResponse<TimerFinishResponse>> completeTimer(
      @PathVariable Long timerId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    TimerFinishResponse response = timerService.completeTimer(userId, timerId);

    return timerResponseFactory.finishResponse(response, TimerSuccessCode.TIMER_COMPLETED);
  }

  @Override
  @PatchMapping("/timers/{timerId}/stop")
  public ResponseEntity<BaseResponse<TimerFinishResponse>> stopTimer(
      @PathVariable Long timerId,
      @AuthenticationPrincipal CustomUserDetails userDetails
  ) {
    Long userId = userDetails.getUserId();
    TimerFinishResponse response = timerService.stopTimer(userId, timerId);

    return timerResponseFactory.finishResponse(response, TimerSuccessCode.TIMER_STOPPED);
  }
}
