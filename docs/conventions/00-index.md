# Timo Server 컨벤션 인덱스

필요한 주제만 읽는다. 여러 영역을 바꾸면 해당 문서를 함께 확인하고, 기존 코드와 규칙이 어긋나면 관련 구현을 먼저 살핀다.

| 문서 | 담당 영역 | 읽는 때 |
| --- | --- | --- |
| [architecture.md](architecture.md) | Java 단일 모듈, 도메인·공통 패키지, 레이어 책임 | 새 도메인·서비스 경계 설계 |
| [coding-style.md](coding-style.md) | 네이밍, Controller·Service·DTO, 검증, API 응답 | Java API 코드 작성·리뷰 |
| [error-handling.md](error-handling.md) | `CustomException`, 에러 코드, `ErrorDto`, Swagger | 예외·오류 응답 변경 |
| [config-and-auth.md](config-and-auth.md) | 프로파일 설정, 환경변수, Spring Security·JWT·OAuth | 설정·인증·인가 변경 |
| [logging.md](logging.md) | `traceId`, MDC, 로깅 필터·Aspect | 로그·추적 변경 |
| [persistence.md](persistence.md) | JPA Entity·관계·트랜잭션, 스키마 변경 | DB 조회·테이블·컬럼 변경 |
| [git-convention.md](git-convention.md) | 이슈·브랜치·커밋·PR·리뷰·배포 | Git 작업 |
| [automation.md](../automation.md) | 요청별 실행 단계·중단 조건·CI와 사람의 판단 지점 | 반복 작업 자동화 설계·점검 |

예: 새로운 할 일 API는 `architecture.md`와 `coding-style.md`를 읽고, 새 컬럼이 있으면 `persistence.md`, 새 오류 코드가 있으면 `error-handling.md`만 추가로 읽는다.

문서 분리 방식은 [DONGCHIMI-SERVER의 CLAUDE.md](https://github.com/TEAM-DONGCHIMI/DONGCHIMI-SERVER/blob/3c51fa59176bca53d40722deed1c712de1042eb9/CLAUDE.md)와 [컨벤션 인덱스](https://github.com/TEAM-DONGCHIMI/DONGCHIMI-SERVER/blob/3c51fa59176bca53d40722deed1c712de1042eb9/docs/conventions/00-index.md)를 참고했다. Kotlin 멀티모듈·Implement Layer·Flyway 규칙은 티모의 현재 구조와 달라 그대로 적용하지 않았다.
