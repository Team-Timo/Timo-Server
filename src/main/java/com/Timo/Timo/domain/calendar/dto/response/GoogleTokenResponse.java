package com.Timo.Timo.domain.calendar.dto.response;

public record GoogleTokenResponse (
  String accessToken,
  String refreshToken,
  Integer expiresIn
){

}
