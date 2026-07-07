package com.Timo.Timo.global.auth.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReissueResult {
  private final String accessToken;
  private final String refreshToken;
  private final String sessionId;
}
