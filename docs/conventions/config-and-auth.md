# 설정과 인증

프로파일 설정·환경변수, JWT·Google OAuth, 인증·인가가 바뀔 때 읽는다.

## 설정 파일

- 공통 설정은 `src/main/resources/application.yml`, 환경별 설정은 `application-local.yml`과 `application-prod.yml`에 있다. 환경별 값이 필요한 키는 두 프로파일과 실제 배포 환경을 함께 확인한다.
- 현재 코드는 `@Value`를 사용한다. 참고 저장소의 Kotlin `@ConfigurationProperties` 전용 규칙이나 모듈별 yml import를 이 저장소에 강제하지 않는다. 설정 항목이 많아져 묶을 필요가 있을 때 Java `@ConfigurationProperties` 도입을 별도로 검토한다.
- `${ENV_VAR}`를 추가할 때는 필요한 프로파일·배포 설정·로컬 실행 방법을 함께 갱신한다. 실제 비밀번호·토큰·키는 문서나 Git에 넣지 않는다. 예시값은 명백한 플레이스홀더만 사용한다.
- CORS 허용 출처는 `global/config/CorsConfig.java`, JWT 유효기간은 `application-*.yml`, 리디렉션 및 쿠키 정책은 `global/auth/`와 프로파일 설정의 조합으로 정해진다. 한 파일만 바꾸고 끝내지 않는다.

## 인증 흐름

`SecurityConfig`가 공개 경로와 인증 필요 경로를 결정한다. `JwtAuthenticationFilter`는 토큰을 검증하고 `CustomUserDetails`를 `SecurityContext`에 넣는다. 보호된 Controller는 `@AuthenticationPrincipal CustomUserDetails`에서 `userId`를 받아 Service에 전달한다. Google OAuth는 `CustomOAuth2UserService`와 성공·실패 handler를 사용한다.

- 새 API를 추가할 때 먼저 공개 API인지 결정한다. 공개 경로는 `SecurityConfig`의 `requestMatchers(...).permitAll()`에 명시하고, 보호 경로는 `anyRequest().authenticated()`를 따른다. 선언 순서와 우회 가능성을 확인한다.
- 사용자가 전달한 `userId`보다 인증 principal의 ID를 신뢰한다. 데이터 조회·수정 시 소유권 검증은 Service/Repository의 기존 패턴을 확인한다.
- JWT·OAuth 토큰, 쿠키, 암호화 키는 응답·로그·PR 본문에 노출하지 않는다. 인증 흐름의 예외 응답은 `global/auth/handler/`와 [error-handling.md](error-handling.md)의 계약을 함께 확인한다.
- 새 권한 체계를 도입하기 전에 현재 `CustomUserDetails`의 `ROLE_USER`와 `SecurityConfig`의 실제 규칙을 확인한다. 참고 저장소의 OWNER/ADMIN/USER role 분리를 그대로 가져오지 않는다.

참고 구현: `global/config/SecurityConfig.java`, `global/jwt/filter/JwtAuthenticationFilter.java`, `global/auth/principal/CustomUserDetails.java`.
