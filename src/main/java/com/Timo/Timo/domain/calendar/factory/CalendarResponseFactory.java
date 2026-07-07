package com.Timo.Timo.domain.calendar.factory;

import com.Timo.Timo.domain.calendar.dto.response.CalendarConnectResponse;
import com.Timo.Timo.domain.calendar.dto.response.CalendarDisconnectResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarSuccessCode;
import com.Timo.Timo.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class CalendarResponseFactory {

  public ResponseEntity<BaseResponse<CalendarConnectResponse>> connectResponse(
      CalendarConnectResponse response
  ) {
    return ResponseEntity.status(CalendarSuccessCode.CALENDAR_CONNECTED.getHttpStatus())
        .body(BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_CONNECTED, response));
  }

  public ResponseEntity<BaseResponse<CalendarDisconnectResponse>> disconnectResponse(
      CalendarDisconnectResponse response
  ) {
    return ResponseEntity.ok(
        BaseResponse.onSuccess(CalendarSuccessCode.CALENDAR_DISCONNECTED, response)
    );
  }
}
