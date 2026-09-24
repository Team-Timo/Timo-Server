# Timo Server 팀 규칙

이 문서는 팀이 합의한 작업 규칙의 원본이다. 오래된 코드나 기존 브랜치가 현재 형식과 다를 수 있으므로, 새 작업에 적용하고 기존 작업을 임의로 이름만 바꾸지 않는다.

## Git 흐름

1. 작업 단위의 이슈를 만든다. 브랜치에 연결된 기존 이슈가 있으면 재사용한다.
2. `develop`에서 작업 브랜치를 만든다. `develop`에서의 일반 코드 작업은 하지 않는다.
3. 변경을 검증하고 관련 파일만 커밋한 뒤 푸시한다.
4. `develop`을 대상으로 PR을 만들고 사람의 코드 리뷰를 받는다.
5. 승인 후 `develop`에 머지한다. 현재 운영 배포는 별도로 `deploy` 브랜치에 머지해야 시작된다.

`main`, `develop`, `deploy`에 직접 푸시하지 않는다. 이미 사용 중인 작업 브랜치는 형식이 다르다는 이유만으로 이름을 바꾸지 않는다.

## 이슈

- 제목: `[<type>] <한국어 요약>` (예: `[feat] 로그인 API 구현`).
- 기능·설정 작업은 [.github/ISSUE_TEMPLATE/server-feature-issue-templete.md](../.github/ISSUE_TEMPLATE/server-feature-issue-templete.md)의 `Feature Issue`와 `Todo`를 채운다. 버그는 재현 조건·실제 결과·기대 결과를 명시한다.
- 담당자를 지정하고 작업 성격에 맞는 라벨을 사용한다. 저장소의 자동 라벨 워크플로가 제목에서 유형 라벨을 붙이므로 실제 라벨명을 확인한 뒤 중복으로 추가하지 않는다.
- 이미 브랜치명이나 대화에 이슈 번호가 있으면 먼저 해당 이슈를 확인한다. 하나의 작업에 같은 이슈를 다시 만들지 않는다.

## 브랜치

- 새 브랜치: `<type>/#<issue-number>-<english-summary>` (예: `fix/#205-todo-date-check`). 설명에 공백을 넣지 않는다.
- 유형: `feat`, `fix`, `refactor`, `chore`, `docs`, `style`, `remove`, `test`, `rename`, `comment`, `hotfix`. 설정 변경에는 현재 저장소에서 쓰는 `setting`도 허용한다.
- 이슈 생성 후 실제 번호로 이름을 정한다. 기존 브랜치가 있으면 재사용하고, 미커밋 변경이 있는 상태에서 임의로 새 브랜치로 옮기지 않는다.

## 커밋

- 형식: `[#<issue-number>] <type>(<scope>): <한국어 요약>`. 여러 클래스를 건드리거나 범위를 특정하기 어려우면 `(<scope>)`를 생략한다.
- 예: `[#205] chore: Git 작업 지침과 스킬 추가`.
- 주요 유형: `feat`, `fix`, `refactor`, `chore`, `docs`, `test`. 필요할 때 브랜치 유형인 `style`, `remove`, `rename`, `comment`, `hotfix`, `setting`도 쓸 수 있다.
- 한 커밋은 하나의 논리적 변경을 담는다. `git add .`로 다른 사람의 변경을 함께 담지 않고 파일을 명시적으로 선택한다.
- 커밋 전 `git diff --check`와 변경 유형에 맞는 검증을 수행한다. Java 변경은 `./gradlew build`를 실행한다. 실패했으면 원인을 해결하거나 실패 사실을 기록하고 성공으로 표시하지 않는다.

## Pull Request와 리뷰

- 제목: `[<type>] #<issue-number> - <한국어 요약>` (예: `[setting] #205 - Git 작업 자동화`).
- 기본 대상: `develop`. 배포용 `deploy` PR은 별도 요청과 운영 절차에서 다룬다.
- 본문: [.github/PULL_REQUEST_TEMPLATE](../.github/PULL_REQUEST_TEMPLATE)의 관련 이슈, 작업 요약, 주요 변경, 트러블 슈팅, 실제 검증 결과, 리뷰 요청 사항을 채운다. 해당 없거나 수행하지 않은 항목은 명확히 표시한다.
- 같은 작업 브랜치에 열린 PR이 있으면 새로 만들지 않고 기존 PR을 갱신한다. 새 PR은 우선 Draft로 제출하고 검증이 끝나면 리뷰 준비 상태로 전환한다.
- 팀은 2명의 코드 리뷰를 완료한 뒤 머지한다. GitHub의 승인 필수 규칙은 저장소 관리자가 Ruleset에서 설정해야 실제로 강제된다.
- 리뷰에서는 존댓말을 사용한다. 받은 리뷰를 확인하면 반응을 남긴다. 서버 톡방 공지는 담당자가 수행하며, 에이전트는 명시적인 전송 요청 없이 메시지를 보내지 않는다.

## Java 및 API 이름

새 코드와 수정한 코드에는 아래 규칙을 적용한다. 기존 도메인과 충돌하면 같은 기능 내의 현재 패턴을 먼저 확인하고 변경 범위를 불필요하게 넓히지 않는다.

- 클래스는 PascalCase, 메서드와 변수는 camelCase, DB 테이블은 snake_case, 상수와 enum 값은 UPPER_SNAKE_CASE다. 컬렉션 변수는 복수형을 사용한다.
- DTO는 `...Request`, `...Response` 접미사를 사용한다. 서비스 메서드는 동작을 드러내는 동사로 시작한다.
- Controller는 조회 `read`, 생성 `create`, 변경 `update`, 삭제 `delete`, 복합 처리 `process`를 우선한다. Service는 조회 `find`, 생성 `generate`/`register`, 변경 `modify`, 삭제 `remove`, 복합 처리 `handle`을 우선한다.
- 공개 메서드를 먼저, 내부 보조 메서드를 그 아래에 둔다. 메서드 하나의 책임과 이름이 어긋나지 않게 한다.
- REST URL은 소문자, 복수형 자원, 하이픈을 사용하고 끝에 `/`를 붙이지 않는다. HTTP 메서드로 표현하는 행위를 URL에 중복해서 쓰지 않는다.
- Entity에서 DB 매핑이 필요한 필드와 관계는 `@Table`, `@Column` 등의 명시적인 매핑을 사용한다. 필드는 기본 정보, 일반 정보, 관계 순서로 정리한다.
- 메서드명은 실제 부수효과를 드러낸다. 예를 들어 조회 중 없으면 생성하는 동작은 `get...`보다 `getOrCreate...`로 표현한다.
- 성공 응답은 기존 `global/response/BaseResponse`의 `status`, `message`, `data` 형식을 따르고, 오류 응답은 기존 `global/exception/dto/ErrorDto`를 따른다. 다른 프로젝트의 DTO 코드를 그대로 복사하지 않는다.
