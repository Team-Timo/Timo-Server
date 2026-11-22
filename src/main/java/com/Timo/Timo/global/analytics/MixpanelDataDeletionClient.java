package com.Timo.Timo.global.analytics;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class MixpanelDataDeletionClient {

  private static final String DATA_DELETION_URL = "https://mixpanel.com/api/app/data-deletions/v3.0/?token={token}";
  private static final String COMPLIANCE_TYPE = "GDPR";
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

  private final String projectToken;
  private final String gdprToken;
  private final RestClient restClient = createRestClient();

  public MixpanelDataDeletionClient(
      @Value("${mixpanel.token:}") String projectToken,
      @Value("${mixpanel.gdpr-token:}") String gdprToken
  ) {
    this.projectToken = projectToken;
    this.gdprToken = gdprToken;
  }

  public boolean isConfigured() {
    return hasText(projectToken) && hasText(gdprToken);
  }

  public void requestDeletion(String distinctId) {
    restClient.post()
        .uri(DATA_DELETION_URL, projectToken)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + gdprToken)
        .contentType(MediaType.APPLICATION_JSON)
        .body(Map.of(
            "distinct_ids", List.of(distinctId),
            "compliance_type", COMPLIANCE_TYPE
        ))
        .retrieve()
        .toBodilessEntity();
  }

  private boolean hasText(String value) {
    return value != null && !value.isBlank();
  }

  private RestClient createRestClient() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
    requestFactory.setReadTimeout(READ_TIMEOUT);

    return RestClient.builder()
        .requestFactory(requestFactory)
        .build();
  }
}
