# 아키텍처

새 도메인·레이어·의존성을 설계할 때 읽는다.

## 현재 구조

- 하나의 Gradle 모듈이며 Java 소스는 `src/main/java/com/Timo/Timo/`에 있다. 참고 저장소의 Kotlin `core/api/infrastructure` 멀티모듈을 가정하지 않는다.
- `domain/{기능}/`: 할 일·타이머·태그·캘린더 등 기능별 Controller, Service, Repository, Entity, DTO, 예외를 함께 둔다.
- `global/`: 공통 인증·JWT, 설정, 응답, 예외, 로깅 등 여러 도메인이 공유하는 기능을 둔다. 한 도메인에서만 쓰는 코드는 해당 도메인에 둔다.
- `TimoApplication.java`가 Spring Boot 진입점이다. 의존성은 루트 `build.gradle`에서 관리한다.

## 의존 방향과 책임

`Controller → Service → Repository/Entity`가 현재 코드의 기본 흐름이다. Controller는 HTTP 입력·응답과 인증 사용자 식별자를 다루고, Service는 비즈니스 규칙·트랜잭션·도메인 간 조율을 맡고, Repository는 영속성 조회와 저장을 맡는다. Entity에는 자신의 상태·불변식을 다루는 메서드를 둘 수 있다.

- Repository가 Service나 Controller를 참조하지 않도록 한다. 공통 코드가 특정 도메인을 참조해야 한다면 해당 책임의 위치를 다시 검토한다.
- 기존 Service가 Repository를 직접 사용하는 구조이므로, 중간 `Reader`·`Appender`·`Manager` 계층을 의무적으로 만들지 않는다. 반복되거나 복잡해진 로직은 해당 도메인 안에서 책임을 나눈다.
- 도메인 간 사용은 필요한 공개 서비스나 조회 기능에 한정하고, 다른 도메인의 내부 DTO·Entity를 직접 조작하는 결합을 늘리지 않는다. 단, 기존 연관관계와 트랜잭션 패턴을 먼저 확인한다.
- 새 패키지나 추상화를 만들기 전에 같은 도메인의 기존 구현을 살핀다. 구조 전체를 바꾸는 리팩토링은 별도 작업으로 다룬다.

참고할 실제 구현: `domain/tag/`의 Controller·Service·Repository·Entity, `global/exception/`, `global/auth/`.
