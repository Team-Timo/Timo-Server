package com.Timo.Timo.global.auth.dto.response;

import com.nimbusds.openid.connect.sdk.claims.UserInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthTokenResponse {

  private final String accessToken;
  private final boolean isNewUser;
  private final UserInfo user;

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
