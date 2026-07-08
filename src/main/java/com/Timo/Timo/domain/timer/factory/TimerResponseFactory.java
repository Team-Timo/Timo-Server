package com.Timo.Timo.domain.timer.factory;

import com.Timo.Timo.domain.timer.dto.response.TimerStartResponse;
import com.Timo.Timo.domain.timer.dto.response.TimerStatusResponse;
import com.Timo.Timo.domain.timer.exception.TimerSuccessCode;
import com.Timo.Timo.global.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class TimerResponseFactory {

  public ResponseEntity<BaseResponse<TimerStartResponse>> startResponse(TimerStartResponse response){
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(BaseResponse.onSuccess(TimerSuccessCode.TIMER_STARTED, response));
  }

  public ResponseEntity<BaseResponse<TimerStatusResponse>> statusResponse(
      TimerStatusResponse response,
      TimerSuccessCode successCode
  ){
    return ResponseEntity.ok()
        .body(BaseResponse.onSuccess(successCode, response));
  }
}
