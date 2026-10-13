package com.Timo.Timo.domain.home.client;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HolidayApiClient {

	private static final String REST_DE_INFO_URL =
		"https://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getRestDeInfo"
			+ "?ServiceKey={serviceKey}&solYear={year}&numOfRows=100&_type=json";
	private static final String SUCCESS_CODE = "00";
	private static final String HOLIDAY_FLAG = "Y";
	private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
	private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

	private final ObjectMapper objectMapper;
	private final RestClient restClient = createRestClient();

	@Value("${holiday.api-key:}")
	private String apiKey;

	public boolean isConfigured() {
		return apiKey != null && !apiKey.isBlank();
	}

	public Set<LocalDate> fetchHolidays(int year) {
		String response;
		try {
			response = restClient.get()
				.uri(REST_DE_INFO_URL, apiKey, year)
				.retrieve()
				.body(String.class);
		} catch (RestClientException e) {
			throw new IllegalStateException(
				"Holiday API request failed. year=" + year
					+ ", cause=" + NestedExceptionUtils.getMostSpecificCause(e).getClass().getSimpleName()
			);
		}

		return extractHolidays(response);
	}

	private RestClient createRestClient() {
		SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
		requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
		requestFactory.setReadTimeout(READ_TIMEOUT);

		return RestClient.builder()
			.requestFactory(requestFactory)
			.build();
	}

	private Set<LocalDate> extractHolidays(String response) {
		JsonNode responseNode = readTree(response).path("response");
		String resultCode = responseNode.path("header").path("resultCode").asText();
		if (!SUCCESS_CODE.equals(resultCode)) {
			throw new IllegalStateException("Holiday API returned an error result.");
		}

		JsonNode itemNode = responseNode.path("body").path("items").path("item");
		Set<LocalDate> holidays = new HashSet<>();
		if (itemNode.isArray()) {
			itemNode.forEach(item -> addHoliday(holidays, item));
		} else if (itemNode.isObject()) {
			addHoliday(holidays, itemNode);
		}
		return holidays;
	}

	private JsonNode readTree(String response) {
		try {
			return objectMapper.readTree(response);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Failed to parse holiday API response.");
		}
	}

	private void addHoliday(Set<LocalDate> holidays, JsonNode item) {
		if (!HOLIDAY_FLAG.equals(item.path("isHoliday").asText())) {
			return;
		}

		try {
			holidays.add(LocalDate.parse(item.path("locdate").asText(), DateTimeFormatter.BASIC_ISO_DATE));
		} catch (DateTimeParseException e) {
			throw new IllegalStateException("Invalid holiday date in API response.");
		}
	}
}
