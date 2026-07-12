package com.Timo.Timo.global.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthReissueResponse {

  @Schema(description = "새로 발급된 AccessToken", example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.def456signature", requiredMode = Schema.RequiredMode.REQUIRED)
  private final String accessToken;
}
