# Timo Server 에이전트 작업 기준

이 파일은 Codex와 Claude가 티모 서버에서 **작업을 실제로 진행할 때** 지킬 공통 기준이다. Java 17·Spring Boot 3.5·Gradle 단일 모듈이며 패키지 루트는 `com.Timo.Timo`다. `CLAUDE.md`는 `@AGENTS.md`로 이 파일을 가져온다.

## 프로젝트 구조

- `src/main/java/com/Timo/Timo/domain/{기능}/`에는 기능별 Controller, Service, Repository, Entity, DTO, 오류 코드를 둔다. 새 기능은 가장 가까운 기존 도메인의 구조와 호출부를 먼저 확인한다.
- `global/`에는 인증·JWT, 설정, 공통 응답·예외, 로깅처럼 여러 도메인이 공유하는 코드를 둔다. 한 도메인만 쓰는 편의 코드를 `global/`에 쌓지 않는다.
- `TimoApplication.java`가 시작점이고 루트 `build.gradle`이 의존성을 관리한다. Kotlin, Spring Modulith, 별도 `core`·`adapter` 모듈을 전제로 패키지를 만들지 않는다.
- 현재 의존 흐름은 `Controller → Service → Repository/Entity`다. HTTP 처리와 입력 검증은 Controller·DTO, 비즈니스 규칙·소유권·트랜잭션은 Service·Entity, 조회·저장은 Repository가 맡는다. Repository가 Service나 Controller를 참조하지 않게 한다.

## 작업 원칙

**구현 전에 확실하지 않은 것은 반드시 사용자에게 질문한다.**

- 요구사항이 여러 방식으로 해석될 수 있으면 임의로 선택하지 말고 선택지를 제시하고 물어본다.
- 컨벤션 문서에 없는 새로운 패턴을 도입해야 하거나 어느 패키지·레이어에 둘지 애매하면, 현재 구조를 확인한 뒤 먼저 물어본다.
- 요청받은 범위만 수정한다. 인접 코드 개선·리팩토링은 임의로 하지 않고, 기존 사용자 변경을 되돌리지 않는다.
- 테스트 코드는 지시하는 사람이 명시적으로 요청하지 않는 이상 작성하지 않는다. 기존 테스트를 실행해 변경을 검증하는 것은 계속 수행한다.

- 변경 전에 `git status --short --branch`로 브랜치와 기존 변경을 확인하고, 관련 코드·호출부·빌드 설정을 읽는다. 새 대화에서도 Git 상태와 연결 이슈·PR을 확인해 중단된 단계부터 이어간다.
- 요구사항이 명확해지면 현재 패키지·의존성을 근거로 가장 작은 변경을 구현·검증한다. 계획만 제시하고 멈추지 않는다.
- 비밀번호·토큰·API 키·인증서, 인증 헤더와 개인정보를 코드·문서·커밋·로그·PR 본문에 노출하지 않는다. 커밋 후보의 비밀정보는 diff를 출력하기 전에 검사한다.
- 현재 작업과 관계없는 문서를 한꺼번에 읽지 않는다. 아래 표에서 해당 주제만 읽고, 여러 영역을 건드릴 때만 문서를 추가한다.
- 구현·커밋·푸시·PR은 각각 별도 단계다. 한 요청에 여러 단계가 들어 있어도 **현재 단계만 수행하고 멈춘다**. 다음 단계는 결과를 확인한 사용자의 새 명령으로 시작한다.

## Java와 API의 핵심 경계

- Controller에는 HTTP 매핑, `@Valid` 요청 검증, 인증 사용자 식별과 응답 변환을 둔다. Service에는 비즈니스 결정과 권한·소유권 확인을 둔다. Entity를 API 응답에 직접 노출하지 않는다.
- 인증이 필요한 API는 현재 `@AuthenticationPrincipal CustomUserDetails`에서 `userId`를 받아 Service에 전달한다. 클라이언트가 보낸 사용자 ID만으로 소유권을 판단하지 않는다. 공개 경로는 `SecurityConfig`의 허용 규칙을 확인한다.
- 새 요청·응답 DTO는 Java `record`와 `...Request`·`...Response` 접미사를 우선한다. URL은 소문자·복수형 자원을 기본으로 하고 HTTP 메서드로 드러나는 동작을 경로에 반복하지 않는다. 기존 도메인의 API 패턴이 다르면 관련 호출부까지 확인한다.
- 클래스는 `PascalCase`, 메서드·변수는 `camelCase`, 상수·enum 값은 `UPPER_SNAKE_CASE`를 따른다. Controller 메서드는 `read/create/update/delete/process`, Service 메서드는 `find/generate/register/modify/remove/handle`를 우선하되 해당 도메인의 기존 이름과 일관성을 확인한다.
- API를 추가·변경하면 기존 `.../docs/{Domain}ControllerDocs` 인터페이스의 OpenAPI 설명과 실제 요청·응답·상태 코드도 맞춘다. 정상 응답은 `BaseResponse(status, message, data)`, 오류는 현재 `ErrorDto` 형식을 따른다.
- 예상 가능한 도메인 실패는 기존 `{Domain}ErrorCode`와 `CustomException`으로 표현한다. 입력 형식은 DTO 검증, 상태·소유권은 Service 또는 Entity에서 검사한다. 내부 예외 메시지나 스택을 클라이언트에 노출하지 않는다.
- Entity·Repository 변경은 기존 JPA 매핑, 트랜잭션, 연관관계 로딩과 N+1 영향을 확인한다. 현재 운영 설정은 `ddl-auto: update`이고 Flyway 마이그레이션 체계가 없으므로 스키마 변경은 데이터·배포 영향을 별도로 검토한다.
- 설정은 `application.yml`, `application-local.yml`, `application-prod.yml`과 관련 Java 설정을 함께 확인한다. 새 환경변수를 추가할 때 로컬·운영 설정을 맞추고 실제 비밀값은 저장소에 넣지 않는다. 현재 코드의 `@Value`를 무조건 다른 바인딩 방식으로 바꾸지 않는다.
- `MdcLoggingFilter`의 `traceId`와 `GlobalExceptionHandler`의 오류 기록을 유지한다. 새 로그에 토큰·쿠키·요청 본문 전체를 넣지 않는다.

## 단계별 실행 흐름

1. **이슈·브랜치 준비**는 “이슈 만들어줘”, “브랜치 만들어줘”, “새 작업 준비해줘”처럼 요청받았을 때만 [timo-issue](.agents/skills/git/timo-issue/SKILL.md)로 진행한다. 기존 이슈·브랜치를 재사용하고, 새 브랜치는 `develop` 기준 `<작업 유형 라벨>/#<번호>-<짧은-영문-내용>`으로 만든다.
2. **구현·검증**은 “구현해줘”, “수정해줘” 요청에서 관련 컨벤션과 기존 도메인 패턴을 확인한 뒤 진행한다. 새 작업에 이슈·작업 브랜치가 없다면 구현 전에 준비 단계를 안내하고 멈춘다. API 문서·오류 코드·보안 규칙과 기존 테스트의 영향을 확인한다. 새 테스트 코드는 명시적 요청이 있을 때만 작성한다. Java 변경은 관련 테스트와 `./gradlew build`, 제출 전에는 `git diff --check`로 확인한다.
3. **커밋**은 “커밋해줘” 요청에서만 [timo-commit](.agents/skills/git/timo-commit/SKILL.md)의 변경 범위·비밀정보 검사를 거쳐 **작은 커밋 계획을 먼저 제시하고 멈춘다**. 사용자가 그 계획을 승인한 다음 요청에서만 계획한 파일을 나누어 커밋한다. 무관한 변경은 포함하지 않는다.
4. **푸시**는 “푸시해줘” 요청과 기존 푸시 금지 지시의 해제가 있을 때만 [timo-push](.agents/skills/git/timo-push/SKILL.md)로 현재 작업 브랜치를 푸시한다. 커밋이나 PR 요청만으로 푸시하지 않는다.
5. **PR**은 “PR 만들어줘” 요청에서만 [timo-pr](.agents/skills/git/timo-pr/SKILL.md)로 이미 푸시된 브랜치의 `develop` 대상 PR을 만들거나 갱신한다. 이 단계에서 커밋·푸시하지 않는다. 완료된 변경은 리뷰 가능한 PR로, 미완료 공유나 Draft 요청은 Draft로 제출한다.
6. **리뷰·배포**에서는 CI 결과와 사람의 코드 리뷰를 확인한다. CodeRabbit은 Draft를 자동 리뷰하지 않으며 사람 2명의 코드 리뷰가 머지 판단의 기준이다. 리뷰는 존댓말로 작성하고 받은 리뷰를 확인했으면 반응을 남긴다. `develop`·`main`·`deploy`에 직접 커밋·푸시하지 않는다. 머지와 `deploy` 배포는 별도 요청·운영 판단을 따른다. 서버 톡방 메시지는 명시적인 전송 요청이 있을 때만 보낸다.

## 작업별 상세 문서

아래 문서는 상세 규칙의 원본이다. **현재 작업에 해당하는 행만 읽는다.** 전체 목록은 [컨벤션 인덱스](docs/conventions/00-index.md)에 있다.

| 작업 | 읽을 문서 |
| --- | --- |
| 새 도메인, 레이어·의존성 설계 | [architecture.md](docs/conventions/architecture.md) |
| Controller·Service·DTO, API 이름·검증 | [coding-style.md](docs/conventions/coding-style.md) |
| 예외, 에러 코드·응답, API 오류 문서화 | [error-handling.md](docs/conventions/error-handling.md) |
| 설정·환경변수, JWT·OAuth·인가 | [config-and-auth.md](docs/conventions/config-and-auth.md) |
| 로그·MDC·요청 추적 | [logging.md](docs/conventions/logging.md) |
| Repository·Entity·JPA 조회, 테이블·컬럼 변경 | [persistence.md](docs/conventions/persistence.md) |
| 이슈·브랜치·커밋·PR·배포 | [git-convention.md](docs/conventions/git-convention.md) |
| 코드 리뷰·품질 점검 | 변경 영역의 위 문서와 [.coderabbit.yaml](.coderabbit.yaml) |
| 자동화 절차를 설계·수정 | [automation.md](docs/automation.md) |

완료 보고에는 바뀐 파일, 실제 실행한 검증과 결과, 실행하지 못한 검증의 이유, 남은 사람의 판단 지점을 한국어로 적는다. 커밋·푸시·PR·머지를 실행하지 않았다면 완료한 것처럼 말하지 않는다.
