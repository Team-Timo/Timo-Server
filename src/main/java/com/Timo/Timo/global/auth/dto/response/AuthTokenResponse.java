package com.Timo.Timo.global.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private final String accessToken;

  @JsonProperty("isNewUser")
  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private final Boolean isNewUser;

  @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
  private final AuthTokenResponse.UserInfo user;

  @Getter
  @Builder
  public static class UserInfo {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String name;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final String email;
    private final String profileImageUrl;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private final boolean onboardingCompleted;
  }
}
