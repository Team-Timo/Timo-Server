# Java·API 코딩 스타일

Controller, Service, Repository, DTO 또는 API를 작성·리뷰할 때 읽는다. 기존 도메인의 실제 이름과 패턴을 먼저 확인하고, 바꾸는 코드에 적용한다.

## 이름과 파일

- 클래스는 `PascalCase`, 메서드·변수는 `camelCase`, 상수·enum 값은 `UPPER_SNAKE_CASE`, DB 테이블·컬럼은 `snake_case`다. 컬렉션 변수는 `users`처럼 복수형을 사용한다.
- 클래스 역할은 `TodoController`, `TodoService`, `TodoRepository`, `TodoCreateRequest`, `TodoCreateResponse`, `TodoErrorCode`처럼 접미사로 드러낸다. 구현체가 실제로 분리될 때만 `Impl`을 붙인다.
- 날짜·시각의 의미를 이름에 드러낸다. 생성·수정 시각은 `createdAt`, `updatedAt`처럼 `At`, 날짜 값은 `...Date`를 우선한다.
- 공개 메서드를 위에, 이를 돕는 `private` 메서드를 아래에 둔다. 이름은 실제 동작과 부수효과를 나타내고 한 메서드가 여러 책임을 감추지 않도록 한다.

## 레이어별 작성

- Controller는 HTTP 매핑, `@Valid` 입력 검증, 인증 사용자 식별, 응답 상태·형식에 집중한다. 인증이 필요한 기존 API는 `@AuthenticationPrincipal CustomUserDetails`에서 `userId`를 꺼내 Service에 전달한다.
- Service는 권한·소유권·비즈니스 규칙을 확인하고 필요한 Repository를 호출한다. 상태 변경은 트랜잭션 안에서, 읽기 전용 조회는 `@Transactional(readOnly = true)`를 기존 도메인처럼 사용한다.
- Repository는 `JpaRepository`와 필요한 조회 메서드·JPQL을 사용한다. 조회 결과가 없을 수 있으면 `Optional` 등으로 명시하고, Controller에 영속성 타입을 직접 노출하지 않는다.
- 요청·응답 DTO는 Java `record`와 `...Request`·`...Response` 접미사를 우선한다. 요청 필드에는 Bean Validation을 붙이고, 중첩 값은 필요할 때 `@Valid`를 사용한다. 복잡한 비즈니스 입력은 Service에 HTTP 전용 타입을 깊이 전달하기보다 의미 있는 값으로 변환할 수 있다. 기존 도메인 전체를 이 이유만으로 일괄 변경하지 않는다.
- 사용자가 읽는 검증 메시지는 한국어로 구체적으로 쓴다. 기존 DTO의 기본 검증 메시지를 바꾸는 작업은 영향 범위를 확인한 뒤 진행한다.
- Entity는 JPA 어노테이션과 도메인 메서드를 가진 Java 클래스로 유지한다. Kotlin `data class`나 별도의 순수 도메인 모델을 의무적으로 만들지 않는다. 자세한 매핑은 [persistence.md](persistence.md)를 본다.

## API와 응답

- URL은 소문자·복수형 자원·하이픈을 사용하고 끝에 `/`를 붙이지 않는다. `GET /api/v1/tags`, `POST /api/v1/tags`처럼 HTTP 메서드로 표현되는 행위를 URL에 반복하지 않는다.
- 새 Controller 메서드는 조회 `read...`, 생성 `create...`, 변경 `update...`, 삭제 `delete...`, 복합 처리 `process...`를 우선한다. Service는 조회 `find...`, 생성 `generate...`/`register...`, 변경 `modify...`, 삭제 `remove...`, 복합 처리 `handle...`를 기준으로 하되 기존 도메인의 공개 이름과 일관성을 먼저 확인한다.
- 성공 응답은 `global/response/BaseResponse`의 `status`, `message`, `data` 형식을 따른다. 실제 HTTP 상태와 `SuccessCode`를 맞추고, 오류 응답은 [error-handling.md](error-handling.md)의 `ErrorDto`를 따른다.
- API 문서는 기존 `.../docs/{Domain}ControllerDocs` 인터페이스와 springdoc 어노테이션 패턴을 확인한다. 구현과 문서의 요청·응답·오류 코드가 어긋나지 않게 한다.

참고 구현: `domain/tag/controller/TagController.java`, `domain/tag/service/TagService.java`, `domain/todo/dto/request/TodoCreateRequest.java`.
