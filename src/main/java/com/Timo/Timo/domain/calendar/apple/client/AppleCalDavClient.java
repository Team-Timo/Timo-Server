package com.Timo.Timo.domain.calendar.apple.client;

import com.Timo.Timo.domain.calendar.exception.CalendarErrorCode;
import com.Timo.Timo.global.exception.CustomException;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

@Slf4j
@Component
public class AppleCalDavClient {

  private static final String ICLOUD_HOST = "icloud.com";
  private static final URI ICLOUD_CALDAV_URL = URI.create("https://caldav.icloud.com/");
  private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
  private static final Duration READ_TIMEOUT = Duration.ofSeconds(10);
  private static final int MAX_REDIRECTS = 3;

  private static final HttpMethod PROPFIND = HttpMethod.valueOf("PROPFIND");
  private static final HttpMethod REPORT = HttpMethod.valueOf("REPORT");
  private static final MediaType XML_UTF8 = new MediaType("application", "xml", StandardCharsets.UTF_8);

  private static final String DAV_NS = "DAV:";
  private static final String CALDAV_NS = "urn:ietf:params:xml:ns:caldav";
  private static final String EVENT_COMPONENT = "VEVENT";
  private static final DateTimeFormatter TIME_RANGE_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC);

  private static final String CURRENT_USER_PRINCIPAL_BODY = """
      <?xml version="1.0" encoding="utf-8"?>
      <d:propfind xmlns:d="DAV:"><d:prop><d:current-user-principal/></d:prop></d:propfind>
      """;
  private static final String CALENDAR_HOME_SET_BODY = """
      <?xml version="1.0" encoding="utf-8"?>
      <d:propfind xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
        <d:prop><c:calendar-home-set/></d:prop>
      </d:propfind>
      """;
  private static final String CALENDAR_LIST_BODY = """
      <?xml version="1.0" encoding="utf-8"?>
      <d:propfind xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
        <d:prop><d:resourcetype/><c:supported-calendar-component-set/></d:prop>
      </d:propfind>
      """;
  private static final String CALENDAR_QUERY_BODY = """
      <?xml version="1.0" encoding="utf-8"?>
      <c:calendar-query xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
        <d:prop><c:calendar-data/></d:prop>
        <c:filter>
          <c:comp-filter name="VCALENDAR">
            <c:comp-filter name="VEVENT"><c:time-range start="%s" end="%s"/></c:comp-filter>
          </c:comp-filter>
        </c:filter>
      </c:calendar-query>
      """;

  private final RestClient restClient = RestClient.builder()
      .requestFactory(buildRequestFactory())
      .build();

  private static JdkClientHttpRequestFactory buildRequestFactory() {
    HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(CONNECT_TIMEOUT)
        .followRedirects(HttpClient.Redirect.NEVER)
        .build();
    JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
    factory.setReadTimeout(READ_TIMEOUT);
    return factory;
  }

  // 계정 주소(principal)를 거쳐 캘린더 목록이 있는 캘린더 홈 주소를 찾는다. 인증 정보가 틀리면 여기서 401이 난다.
  public String discoverCalendarHomeUrl(String appleId, String appPassword) {
    String authorization = toBasicAuthorization(appleId, appPassword);

    try {
      CalDavResponse principalResponse = send(PROPFIND, ICLOUD_CALDAV_URL, "0", CURRENT_USER_PRINCIPAL_BODY, authorization);
      URI principalUrl = findHref(principalResponse, DAV_NS, "current-user-principal");

      CalDavResponse homeResponse = send(PROPFIND, principalUrl, "0", CALENDAR_HOME_SET_BODY, authorization);
      return findHref(homeResponse, CALDAV_NS, "calendar-home-set").toString();
    } catch (CalDavNotFoundException exception) {
      log.warn("애플 캘린더 계정 주소를 찾지 못했습니다.");
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
    }
  }

  // 일정(VEVENT)을 담는 캘린더 주소 목록. 저장한 캘린더 홈 주소가 더 이상 없으면 빈 값을 돌려준다.
  public Optional<List<URI>> findEventCalendarUrls(String appleId, String appPassword, String calendarHomeUrl) {
    CalDavResponse response;
    try {
      response = send(PROPFIND, URI.create(calendarHomeUrl), "1", CALENDAR_LIST_BODY, toBasicAuthorization(appleId, appPassword));
    } catch (CalDavNotFoundException exception) {
      return Optional.empty();
    }

    List<URI> calendarUrls = new ArrayList<>();
    for (Element responseElement : elements(parseXml(response.body()).getDocumentElement(), DAV_NS, "response")) {
      Element prop = findSuccessProp(responseElement);
      if (prop == null || !isEventCalendar(prop)) {
        continue;
      }
      String href = textOf(responseElement, DAV_NS, "href");
      if (href != null) {
        calendarUrls.add(response.url().resolve(href));
      }
    }
    return Optional.of(calendarUrls);
  }

  // 조회 기간과 겹치는 일정 리소스의 iCalendar 원문 목록
  public List<String> fetchCalendarData(String appleId, String appPassword, URI calendarUrl, Instant timeMin, Instant timeMax) {
    String body = CALENDAR_QUERY_BODY.formatted(TIME_RANGE_FORMAT.format(timeMin), TIME_RANGE_FORMAT.format(timeMax));
    CalDavResponse response;
    try {
      response = send(REPORT, calendarUrl, "1", body, toBasicAuthorization(appleId, appPassword));
    } catch (CalDavNotFoundException exception) {
      // 일정이 없는 것으로 보면 동기화가 기존 투두를 삭제하므로 실패로 처리한다.
      log.warn("애플 캘린더 일정 조회 대상 캘린더를 찾지 못했습니다.");
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
    }

    List<String> calendarData = new ArrayList<>();
    for (Element responseElement : elements(parseXml(response.body()).getDocumentElement(), DAV_NS, "response")) {
      Element prop = findSuccessProp(responseElement);
      String data = prop != null ? textOf(prop, CALDAV_NS, "calendar-data") : null;
      if (data != null && !data.isBlank()) {
        calendarData.add(data);
      }
    }
    return calendarData;
  }

  private CalDavResponse send(HttpMethod method, URI url, String depth, String body, String authorization) {
    URI requestUrl = url;
    for (int redirectCount = 0; redirectCount <= MAX_REDIRECTS; redirectCount++) {
      validateAppleUrl(requestUrl);
      CalDavResponse response = exchange(method, requestUrl, depth, body, authorization);
      int status = response.status();
      if (status == 207 || (status >= 200 && status < 300)) {
        return response;
      }
      if (status >= 300 && status < 400 && response.location() != null) {
        requestUrl = requestUrl.resolve(response.location());
        continue;
      }
      throw toException(status);
    }
    log.warn("애플 캘린더 리다이렉트 횟수 초과. method={}", method);
    throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
  }

  // 응답의 href·Location이나 저장된 주소가 다른 곳을 가리켜도 인증 정보가 iCloud 밖으로 나가지 않게 한다.
  private void validateAppleUrl(URI url) {
    String host = url.getHost() != null ? url.getHost().toLowerCase(Locale.ROOT) : null;
    boolean appleHost = host != null && (host.equals(ICLOUD_HOST) || host.endsWith("." + ICLOUD_HOST));
    if (!"https".equalsIgnoreCase(url.getScheme()) || !appleHost) {
      log.warn("애플 캘린더 요청 주소가 iCloud HTTPS 주소가 아닙니다. host={}", host);
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
    }
  }

  private CalDavResponse exchange(HttpMethod method, URI url, String depth, String body, String authorization) {
    try {
      return restClient.method(method)
          .uri(url)
          .header(HttpHeaders.AUTHORIZATION, authorization)
          .header("Depth", depth)
          .contentType(XML_UTF8)
          .body(body)
          .exchange((request, response) -> new CalDavResponse(
              url,
              response.getStatusCode().value(),
              response.getHeaders().getLocation(),
              StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8)
          ));
    } catch (ResourceAccessException exception) {
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_TIMEOUT);
    } catch (RuntimeException exception) {
      log.warn("애플 캘린더 요청 실패. method={}, reason={}", method, exception.getClass().getSimpleName());
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
    }
  }

  private RuntimeException toException(int status) {
    if (status == 401 || status == 403) {
      return new CustomException(CalendarErrorCode.APPLE_CALENDAR_AUTH_FAILED);
    }
    if (status == 404) {
      return new CalDavNotFoundException();
    }
    if (status == 429) {
      return new CustomException(CalendarErrorCode.APPLE_CALENDAR_RATE_LIMITED);
    }
    log.warn("애플 캘린더 응답 오류. status={}", status);
    return new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
  }

  private URI findHref(CalDavResponse response, String namespace, String propertyName) {
    Document document = parseXml(response.body());
    for (Element property : elements(document.getDocumentElement(), namespace, propertyName)) {
      String href = textOf(property, DAV_NS, "href");
      if (href != null && !href.isBlank()) {
        return response.url().resolve(href.strip());
      }
    }
    log.warn("애플 캘린더 응답에 {} 주소가 없습니다.", propertyName);
    throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
  }

  private boolean isEventCalendar(Element prop) {
    Element resourceType = firstElement(prop, DAV_NS, "resourcetype");
    if (resourceType == null || firstElement(resourceType, CALDAV_NS, "calendar") == null) {
      return false;
    }
    Element componentSet = firstElement(prop, CALDAV_NS, "supported-calendar-component-set");
    if (componentSet == null) {
      return true;
    }
    return elements(componentSet, CALDAV_NS, "comp").stream()
        .anyMatch(component -> EVENT_COMPONENT.equalsIgnoreCase(component.getAttribute("name")));
  }

  // 여러 propstat 중 200 상태의 prop만 사용한다.
  private Element findSuccessProp(Element responseElement) {
    for (Element propStat : elements(responseElement, DAV_NS, "propstat")) {
      if (parseStatusCode(textOf(propStat, DAV_NS, "status")) == 200) {
        return firstElement(propStat, DAV_NS, "prop");
      }
    }
    return null;
  }

  // "HTTP/1.1 200 OK" 형식에서 상태 코드 숫자만 꺼낸다.
  private int parseStatusCode(String statusLine) {
    if (statusLine == null) {
      return -1;
    }
    String[] parts = statusLine.strip().split("\\s+");
    if (parts.length < 2) {
      return -1;
    }
    try {
      return Integer.parseInt(parts[1]);
    } catch (NumberFormatException exception) {
      return -1;
    }
  }

  // 외부 엔티티·DTD를 막아 XXE를 방어한다.
  private Document parseXml(String xml) {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
      factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
      factory.setXIncludeAware(false);
      factory.setExpandEntityReferences(false);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception exception) {
      log.warn("애플 캘린더 XML 응답 파싱 실패. reason={}", exception.getClass().getSimpleName());
      throw new CustomException(CalendarErrorCode.APPLE_CALENDAR_UPSTREAM_ERROR);
    }
  }

  private List<Element> elements(Element parent, String namespace, String localName) {
    NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
    List<Element> result = new ArrayList<>(nodes.getLength());
    for (int i = 0; i < nodes.getLength(); i++) {
      result.add((Element) nodes.item(i));
    }
    return result;
  }

  private Element firstElement(Element parent, String namespace, String localName) {
    NodeList nodes = parent.getElementsByTagNameNS(namespace, localName);
    return nodes.getLength() > 0 ? (Element) nodes.item(0) : null;
  }

  private String textOf(Element parent, String namespace, String localName) {
    Element element = firstElement(parent, namespace, localName);
    return element != null ? element.getTextContent() : null;
  }

  private String toBasicAuthorization(String appleId, String appPassword) {
    String credentials = appleId + ":" + appPassword;
    return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
  }

  private record CalDavResponse(URI url, int status, URI location, String body) {

  }

  private static class CalDavNotFoundException extends RuntimeException {

  }
}
