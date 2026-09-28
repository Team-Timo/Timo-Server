# Timo Server Git 작업 자동화 운영

`AGENTS.md`는 매 작업의 실행 순서와 권한 경계를 정하고, `docs/conventions/`는 주제별 팀 규칙을 담는다. `.agents/skills/git/`는 이슈·커밋·푸시·PR의 **별도 단계**를 맡는다. 새 대화에서는 현재 Git·GitHub 상태를 확인해 중단된 단계부터 이어가고, 작업과 관계없는 문서는 읽지 않는다. `CLAUDE.md`는 `@AGENTS.md`로 공통 안내를 가져오며, Claude도 안내에 연결된 절차를 필요할 때 읽는다. Claude 전용 `/timo-*` 명령 자동 탐색은 제공하지 않는다.

## 작업별 실행 경계

| 단계 | 에이전트가 처리할 일 | 사람이 확인할 지점 |
| --- | --- | --- |
| 이슈·브랜치 준비 | 기존 항목을 재사용하고 필요할 때만 이슈 생성과 연결 브랜치 checkout | 구현으로 이어갈지 결정 |
| 구현·검증 | 관련 코드·규칙 확인, 필요한 변경과 검증 수행 | 변경 내용과 검증 결과 확인 |
| 커밋 계획 | 비밀정보 검사, 작은 커밋별 파일·hunk·메시지 제시 | 계획 수정, 로컬 커밋 또는 푸시 선택 |
| 커밋 실행 | 승인 뒤 변경이 없으면 계획한 단위만 로컬 커밋 | 커밋만 요청했다면 푸시 여부 결정 |
| 푸시 | 직전 계획이 있으면 승인된 커밋을 만든 뒤, 없으면 기존 로컬 커밋만 현재 작업 브랜치에 일반 푸시 | PR 제출 여부 결정 |
| PR | 기존 PR 재사용 또는 `develop` 대상 PR 생성·갱신, CI 상태 확인 | 리뷰·머지 판단 |
| 배포 | `deploy` 대상 PR과 배포 워크플로 상태 확인 | 운영 반영 결정과 `deploy` 머지 |

각 단계는 실제 상태를 조회한 뒤 한 번만 수행한다. 기존 이슈·연결 브랜치·열린 PR이 있으면 중복 생성하지 않고, 새 대화에서도 Git과 GitHub 상태를 작업 기록으로 사용한다. 한 요청에 여러 단계가 포함되어도 가장 앞의 미완료 단계만 수행한다. 다만 커밋 계획을 확인한 다음 요청의 “푸시해줘”는 계획 승인·커밋·푸시를 하나의 연속 단계로 처리한다.

단계별 트리거와 실행 방법은 [timo-issue](../.agents/skills/git/timo-issue/SKILL.md), [timo-commit](../.agents/skills/git/timo-commit/SKILL.md), [timo-push](../.agents/skills/git/timo-push/SKILL.md), [timo-pr](../.agents/skills/git/timo-pr/SKILL.md)를 원본으로 삼는다. 이슈·브랜치·커밋·PR의 이름과 형식은 [git-convention.md](conventions/git-convention.md)에만 정의한다.

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
