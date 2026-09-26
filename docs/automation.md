# Timo Server Git 작업 자동화 운영

`AGENTS.md`는 매 작업의 실행 순서와 권한 경계를 정하고, `docs/conventions/`는 주제별 팀 규칙을 담는다. `.agents/skills/git/`는 이슈·커밋·푸시·PR의 **별도 단계**를 맡는다. 새 대화에서는 현재 Git·GitHub 상태를 확인해 중단된 단계부터 이어가고, 작업과 관계없는 문서는 읽지 않는다. `CLAUDE.md`는 `@AGENTS.md`로 공통 안내를 가져오며, Claude도 안내에 연결된 절차를 필요할 때 읽는다. Claude 전용 `/timo-*` 명령 자동 탐색은 제공하지 않는다.

## 작업별 실행 경계

| 요청 | 자동으로 처리할 일 | 사람이 확인할 지점 |
| --- | --- | --- |
| “새 작업 준비해줘” | 기존 이슈·브랜치를 확인하고 필요할 때만 이슈 생성·연결 브랜치 checkout | 구현·커밋으로 이어가지 않음 |
| “구현해줘”, “수정해줘” | 관련 코드·규칙 확인, 필요한 Java/API 문서 변경, 기존 테스트·빌드 검증 | 새 이슈·브랜치가 필요하면 먼저 준비 명령 요청 |
| “커밋해줘”, “staged 변경 정리해줘” | 비밀정보 검사, 작은 커밋별 파일·hunk·메시지 계획 제시 | 계획 승인 전에는 스테이징·커밋하지 않음 |
| “커밋 계획 승인” | 계획 후 변경이 없으면 각 단위를 선별해 로컬 커밋 | 푸시·PR로 이어가지 않음 |
| “푸시해줘” | 검증된 로컬 커밋만 현재 작업 브랜치에 일반 푸시 | 푸시 금지 지시가 남아 있으면 멈춤 |
| “PR 만들어줘” | 이미 푸시된 브랜치에서 기존 PR 재사용 또는 `develop` 대상 PR 생성, CI 상태 확인 | 미커밋·미푸시 변경을 대신 처리하지 않음 |
| “PR 본문 초안 써줘” | 변경 요약·검증 결과·위험을 템플릿에 맞춰 로컬에서 작성 | GitHub PR은 만들지 않음 |
| 배포 | `deploy` 대상 PR과 배포 워크플로 상태 확인 | 운영 반영 결정과 `deploy` 머지 |

각 단계는 실제 상태를 조회한 뒤 한 번만 수행한다. 예를 들어 `#205`가 이미 있으면 같은 이슈를 만들지 않고, 연결 브랜치와 현재 checkout을 확인한다. 열린 PR이 있으면 중복 생성하지 않고 실제 diff에 맞게 갱신한다. 새 대화에서도 Git 상태·이슈·PR을 이어받을 작업 기록으로 사용한다. **한 메시지에 구현·커밋·푸시·PR 요청이 모두 있어도 가장 앞의 미완료 단계만 수행하고 결과를 보고한다.** 다음 단계는 사용자가 결과를 확인하고 새 명령을 보낸 뒤 시작한다.

커밋 계획 승인만 이번 작업 파일을 선별해 스테이징·커밋할 권한이다. 기존의 무관한 변경은 포함하지 않는다. staged 변경 정리만 요청받았다면 새 파일을 추가하지 않는다. 실제 자격 증명이 의심되면 값 대신 파일명만 알리고 멈춘다. PR 요청은 커밋·푸시 승인이 아니다.

예를 들어 “이번 변경 커밋하고 PR 올려줘”에는 작은 커밋 계획만 보여주고 멈춘다. “커밋 계획 승인” 후 커밋하고 멈추며, 이후 “푸시해줘”와 “PR 만들어줘”가 각각 있어야 다음 단계로 간다. 이전의 “푸시하지마”가 남아 있으면 명시적으로 해제되기 전까지 푸시하지 않는다.

## PR 검증과 리뷰

`.github/workflows/pr-check.yml`은 GitHub에 반영된 뒤 `develop` 또는 `deploy` 대상 PR에서 JDK 17로 `./gradlew build`를 실행한다. 현재 저장소에는 `src/test` 테스트 파일이 없으므로 이 체크는 **현재는 빌드 확인**이 중심이며, 테스트가 추가되면 같은 Gradle 작업에서 실행된다. 파일 경로 필터를 두지 않아 문서 PR에서도 체크 이름이 안정적으로 나타난다. 로컬 검증 결과와 CI 결과를 PR 본문에 구분해 적는다. 로컬에만 있는 워크플로 파일을 실제 CI가 실행된 것으로 보고하지 않는다.

GitHub 저장소 관리자에게 `develop`과 `deploy`의 Ruleset 또는 브랜치 보호에 **PR 필수, `PR checks / build` 필수, 승인 리뷰 2명, 대화 해결**을 설정하도록 요청한다. 저장소 안의 문서나 Slack 승인 알림만으로는 머지를 강제할 수 없다. 현재 `.coderabbit.yaml`은 `develop` 대상에서 Draft 자동 리뷰를 끄고 Markdown 파일도 리뷰 대상에서 제외한다. 따라서 구현·로컬 검증이 끝난 PR 제출 요청은 리뷰 가능한 상태로 만들고, 미완료 작업 공유나 사용자의 Draft 요청일 때만 Draft를 사용한다. Draft를 리뷰받으려면 검증 후 리뷰 준비 상태로 전환해야 한다. CI 실패나 리뷰 수정 요청은 이 PR 단계에서 자동으로 코드 수정·커밋·푸시하지 않고 다음 구현 명령으로 다룬다. CodeRabbit은 참고 자료이고 최종 리뷰 판단은 팀원이 한다. 운영 배포는 기존처럼 `deploy` 브랜치에 머지될 때 시작되므로 PR 자동 생성이나 리뷰 완료가 배포를 뜻하지 않는다.

현재 로컬의 `origin/HEAD`는 `develop`을 가리킨다. PR을 작성할 때 GitHub의 실제 기본 브랜치를 다시 확인한다. GitHub의 `closes #번호`는 **기본 브랜치를 대상으로 하는 PR**에서만 이슈 연결·자동 종료에 사용된다. 기본 브랜치가 다르면 이슈를 수동으로 연결하고 자동 종료를 전제하지 않는다.

## 참고 자료와 적용 이유

- [Claude Code Docs: How Claude remembers your project](https://code.claude.com/docs/en/memory): `CLAUDE.md`의 `@AGENTS.md` import와 필요할 때만 추가 문서를 읽는 구성을 확인했다.
- [Claude Code Docs: Extend Claude with skills](https://code.claude.com/docs/en/skills): `.claude/skills/`가 Claude 전용 스킬 자동 탐색 위치라는 점을 확인하고, 이번 저장소에서는 중복 래퍼를 제거했다.
- [OpenAI Docs: Build skills](https://learn.chatgpt.com/docs/build-skills): Codex의 저장소 스킬 경로 `.agents/skills/`를 확인했다.
- [GitHub Docs: Creating an issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/creating-an-issue): 이슈 제목·본문·담당자·라벨을 CLI에서도 설정할 수 있어 템플릿 기반 반복 작업에 적용했다.
- [GitHub Docs: Creating a branch for an issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/creating-a-branch-for-an-issue), [GitHub CLI: gh issue develop](https://cli.github.com/manual/gh_issue_develop): 이슈에 연결된 브랜치를 `develop` 기준으로 생성하고 로컬 checkout까지 진행하는 절차를 반영했다.
- [GitHub CLI: gh pr create](https://cli.github.com/manual/gh_pr_create): `--base`, `--head`, `--draft`, `--body-file`로 PR 대상을 명시하고 본문을 안정적으로 제출하는 절차를 반영했다.
- [GitHub Docs: Building and testing Java with Gradle](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle): PR에서 Java 빌드·테스트를 실행하는 CI와 Gradle 캐시 구성을 참고했다.
- [GitHub Docs: Managing protected branches](https://docs.github.com/en/repositories/configuring-branches-and-merges-in-your-repository/managing-protected-branches): 리뷰 수와 필수 상태 검사는 저장소 Ruleset 또는 브랜치 보호에서 강제해야 한다는 점을 반영했다.
- [GitHub Docs: Linking a pull request to an issue](https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/linking-a-pull-request-to-an-issue): 기본 브랜치가 아닌 PR에는 closing keyword가 동작하지 않는 조건을 반영했다.
- [GitHub Docs: Push protection](https://docs.github.com/en/code-security/concepts/secret-security/push-protection): 서버의 원격 푸시 보호와 별개로, 커밋 전에 staged 비밀정보를 검사하도록 했다.
