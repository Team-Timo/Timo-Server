package com.Timo.Timo.domain.calendar.client;

import com.Timo.Timo.domain.calendar.dto.client.CalendarEventItem;
import com.Timo.Timo.domain.calendar.dto.response.CalendarEventsResponse;
import com.Timo.Timo.domain.calendar.dto.client.GoogleTokenResponse;
import com.Timo.Timo.domain.calendar.dto.client.GoogleUserInfoResponse;
import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class GoogleOAuthClient {

  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);

  private final RestClient restClient = RestClient.builder()
      .requestFactory(buildRequestFactory())
      .build();

  @Value("${spring.security.oauth2.client.registration.google.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.google.client-secret}")
  private String clientSecret;

  @Value("${app.calendar.redirect-uri}")
  private String redirectUri;

  private static SimpleClientHttpRequestFactory buildRequestFactory() {
    SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(CONNECT_TIMEOUT);
    factory.setReadTimeout(READ_TIMEOUT);
    return factory;
  }

  public GoogleTokenResponse exchangeToken(String authorizationCode){
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("code", authorizationCode);
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("redirect_uri", redirectUri);
    body.add("grant_type", "authorization_code");

    try {
      return restClient.post()
          .uri("https://oauth2.googleapis.com/token")
          .body(body)
          .retrieve()
          .body(GoogleTokenResponse.class);
    } catch (ResourceAccessException e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_TIMEOUT);
    } catch (RestClientResponseException e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    } catch (Exception e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    }
  }

  public GoogleTokenResponse refreshAccessToken(String refreshToken) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("client_id", clientId);
    body.add("client_secret", clientSecret);
    body.add("refresh_token", refreshToken);
    body.add("grant_type", "refresh_token");

    try {
      return restClient.post()
          .uri("https://oauth2.googleapis.com/token")
          .body(body)
          .retrieve()
          .body(GoogleTokenResponse.class);
    } catch (Exception e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    }
  }


  public List<CalendarEventItem> fetchEvents(String accessToken, Instant timeMin, Instant timeMax) {
    String uri = UriComponentsBuilder
        .fromUriString("https://www.googleapis.com/calendar/v3/calendars/primary/events")
        .queryParam("timeMin", timeMin.toString())
        .queryParam("timeMax", timeMax.toString())
        .queryParam("singleEvents", true)
        .queryParam("orderBy", "startTime")
        .queryParam("maxResults", 100)
        .toUriString();

    try {
      CalendarEventsResponse response = restClient.get()
          .uri(uri)
          .header("Authorization", "Bearer " + accessToken)
          .retrieve()
          .body(CalendarEventsResponse.class);

      return response.items() != null ? response.items() : List.of();
    } catch (Exception e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    }
  }

  public GoogleUserInfoResponse fetchUserInfo(String accessToken){
    try {
      return restClient.get()
          .uri("https://www.googleapis.com/oauth2/v2/userinfo")
          .header("Authorization", "Bearer " + accessToken)
          .retrieve()
          .body(GoogleUserInfoResponse.class);
    } catch (ResourceAccessException e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_TIMEOUT);
    } catch (RestClientResponseException e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    } catch (Exception e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_AUTH_FAILED);
    }
  }

  public void revokeToken(String token) {
    MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
    body.add("token", token);

    try {
      restClient.post()
          .uri("https://oauth2.googleapis.com/revoke")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(body)
          .retrieve()
          .toBodilessEntity();
    } catch (ResourceAccessException e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_TIMEOUT);
    } catch (Exception e) {
      throw new CustomException(CalendarErrorCode.CALENDAR_REVOKE_FAILED);
    }
  }
}
