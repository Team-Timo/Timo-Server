# 로깅과 요청 추적

MDC, 필터, 서비스 로그 또는 오류 로그를 바꿀 때 읽는다.

- `MdcLoggingFilter`는 요청의 `X-Trace-Id`가 허용 형식이면 사용하고, 없거나 형식이 다르면 새 ID를 만든다. 응답 헤더에도 ID를 넣고 MDC에 `traceId`, HTTP method, URI를 기록한다.
- 필터는 시작·완료 로그와 상태·소요 시간을 기록하고 `finally`에서 `MDC.clear()`를 호출한다. `SecurityConfig`가 필터 순서를 정하며 중복 Servlet 등록은 꺼 둔다. 변경 시 이 두 경로를 함께 확인한다.
- `LoggingAspect`는 Service 완료 시간과 예외를 기록한다. 예상된 `CustomException`은 WARN, 예상하지 못한 예외는 ERROR로 다루고 원래 예외를 다시 던진다. `GlobalExceptionHandler`의 로그 및 Sentry 기록과 중복·민감정보 노출을 검토한다.
- 로그 출력 패턴은 `src/main/resources/logback-spring.xml`에 있다. 현재 콘솔 패턴 로그이며 참고 저장소의 JSON encoder, 비동기 appender, `requestId`/`userId` MDC 체계를 이미 적용한 것으로 가정하지 않는다.
- 토큰·비밀번호·쿠키·API 키, 인증 헤더, 요청·응답 본문 전체를 로그에 넣지 않는다. 새 로그 필드는 문제 분석에 필요한 최소 정보로 제한한다.

참고 구현: `global/logging/MdcLoggingFilter.java`, `global/logging/LoggingAspect.java`, `global/logging/LoggingConstants.java`.
