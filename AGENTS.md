# Timo Server 작업 안내

- Java 17, Spring Boot, Gradle 서버다. 변경 범위와 현재 Git 상태를 먼저 확인한다.
- 기능 코드는 `src/main/java/com/Timo/Timo/domain/`, 공통 코드는 `global/`에서 찾는다. 기존 도메인의 구현 패턴을 확인한 뒤 필요한 파일만 읽는다.
- 팀의 Git 및 코드 규칙은 [docs/rules.md](docs/rules.md)에 있다. Git 작업 전에는 Git 규칙을, Java 코드를 수정할 때는 코드 규칙을 확인한다.
- 이슈가 이미 브랜치명에 연결되어 있으면 해당 이슈를 확인하고 재사용한다.
- 일반 작업은 이슈 → 기능 브랜치 → 검증 → 커밋 → 푸시 → `develop` 대상 PR → 사람의 코드 리뷰 순서다. `develop`, `main`, `deploy`에 직접 커밋하거나 푸시하지 않는다.
- Java 변경은 `./gradlew build`로 검증한다. 실행하지 않은 테스트를 통과했다고 쓰지 않는다. 변경 제출 전 `git diff --check`도 실행한다.
- PR은 기존 [.github/PULL_REQUEST_TEMPLATE](.github/PULL_REQUEST_TEMPLATE)을 채운다. 리뷰는 사람이 진행하고, 운영 배포는 별도의 승인·머지 절차를 따른다.
- 반복 작업: 이슈는 `timo-issue`, 커밋은 `timo-commit`, 푸시와 PR은 `timo-pr` 스킬을 사용한다. 스킬은 현재 요청과 관련된 경우에만 읽는다.
