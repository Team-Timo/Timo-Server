# 에러 처리와 API 오류 문서

새 에러 코드, 검증 오류, 예외 응답이나 Swagger 오류 설명을 바꿀 때 읽는다.

## 현재 계약

- 공통·도메인 오류는 `BaseErrorCode`를 구현한 `ErrorCode` 또는 `{Domain}ErrorCode` enum으로 정의한다. 각 값의 HTTP 상태·코드·사용자 메시지를 함께 검토한다.
- 예측 가능한 비즈니스 실패는 `new CustomException(errorCode)`로 표현한다. `GlobalExceptionHandler`가 이를 `ErrorDto`로 변환한다. 한 도메인의 입력 오류에 특별한 메시지가 필요하면 기존 `TodoExceptionHandler`, `TagExceptionHandler`처럼 범위를 제한한 handler를 확인한다.
- 오류 JSON은 `timestamp`, `status`, `errorCode`, `message`, `path`, `traceId`를 포함한다. 새 오류에 별도 응답 래퍼를 만들지 않는다. 정상 응답의 `BaseResponse`와 형식이 다르다는 점도 유지한다.
- 요청 검증 실패, 읽을 수 없는 본문, 지원하지 않는 HTTP 메서드, 동시성 충돌 등은 `GlobalExceptionHandler`가 이미 처리한다. 신규 handler를 추가할 때 기존 handler와 우선순위·중복 매핑을 확인한다.
- 예상하지 못한 오류는 서버 로그와 Sentry에 남기고 클라이언트에는 내부 예외의 메시지·스택·비밀값을 노출하지 않는다. 처리 가능한 비즈니스 오류는 적절한 4xx를 반환하고 500으로 뭉개지 않는다.

## 새 오류를 추가할 때

1. 같은 도메인에 재사용 가능한 코드가 있는지 확인한다. 없으면 해당 `{Domain}ErrorCode`에 추가하고, 코드 식별자·상태·메시지가 서로 일치하는지 검토한다.
2. 오류가 나는 위치에서 `CustomException`을 던진다. 입력 형식 검증은 가능하면 DTO의 Bean Validation에 두고, 소유권·상태 검증은 Service 또는 Entity의 책임에 둔다.
3. 기존 `...ControllerDocs`의 응답 설명과 실제 `ErrorDto`를 맞춘다. 해당 API가 실제로 내는 코드만 문서화하고, 사용하지 않는 enum 전체를 기계적으로 나열하지 않는다.
4. 로그에는 오류 코드·경로·`traceId`처럼 진단에 필요한 정보만 남긴다. 토큰·비밀번호·개인정보나 요청 본문 전체를 기록하지 않는다.

참고 구현: `global/exception/GlobalExceptionHandler.java`, `global/exception/dto/ErrorDto.java`, `domain/tag/exception/TagErrorCode.java`.
