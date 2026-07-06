package com.Timo.Timo.global.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {

  private final String accessToken;

  @JsonProperty("isNewUser")
  private final Boolean isNewUser;

  private final AuthTokenResponse.UserInfo user;

  @Getter
  @Builder
  public static class UserInfo {
    private final Long id;
    private final String name;
    private final String email;
    private final String profileImageUrl;
    private final boolean onboardingCompleted;
  }

}
