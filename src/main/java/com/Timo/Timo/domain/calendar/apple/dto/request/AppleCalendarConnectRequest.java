package com.Timo.Timo.domain.calendar.apple.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AppleCalendarConnectRequest(
    @NotBlank(message = "appleId는 필수입니다.")
    @Email(message = "appleId는 이메일 형식이어야 합니다.")
    @Schema(description = "Apple ID 이메일", example = "timo@icloud.com", requiredMode = Schema.RequiredMode.REQUIRED)
    String appleId,

    @NotBlank(message = "appPassword는 필수입니다.")
    @Schema(description = "account.apple.com에서 발급한 앱 전용 암호", example = "abcd-efgh-ijkl-mnop", requiredMode = Schema.RequiredMode.REQUIRED)
    String appPassword
) {

  @Override
  public String toString() {
    return "AppleCalendarConnectRequest[appleId=" + appleId + ", appPassword=****]";
  }
}
