# Git·이슈·PR 컨벤션

이슈, 브랜치, 커밋, PR 또는 배포 흐름을 다룰 때 읽는다. 기존 브랜치·커밋을 형식만 맞추려고 다시 쓰지 않는다.

## 작업 흐름

1. 작업 단위 이슈를 만들되, 브랜치명이나 요청에 연결된 기존 이슈가 있으면 재사용한다.
2. 이슈 번호와 작업 유형 라벨을 확인한 뒤, **그 이슈에 연결된 브랜치**를 `develop` 기준으로 생성하고 해당 브랜치로 checkout한다. 일반 코드를 `develop`에서 직접 작업하거나 `main`·`develop`·`deploy`에 직접 커밋·푸시하지 않는다.
3. 구현·검증 뒤 멈춘다. 커밋 명령에는 작은 커밋 계획을 먼저 보여주고 승인 후 커밋한다. 푸시와 PR은 각각 별도의 명령이 있어야 한다.
4. 이미 푸시된 브랜치에서 `develop` 대상 PR을 만든다. CI와 사람의 코드 리뷰를 받고, 승인 후 머지한다. 운영 반영은 별도의 `deploy` 브랜치 머지가 트리거한다.

## 이슈와 브랜치

- 이슈 제목: `[<type>] <한국어 요약>` (예: `[feat] 로그인 API 구현`). 기능·설정은 [feature 템플릿](../../.github/ISSUE_TEMPLATE/server-feature-issue-templete.md), 버그는 [bug 템플릿](../../.github/ISSUE_TEMPLATE/server-bug-issue.md)을 사용한다. 목적과 확인 가능한 Todo를 적고, 버그에는 재현·실제·기대 결과를 포함한다.
- 담당자와 실제 저장소 라벨을 확인한다. 자동 라벨 워크플로가 제목에서 유형을 붙일 수 있으므로 중복 추가하지 않는다.
- 새 브랜치: `<라벨>/#<이슈번호>-<내용>` (예: `fix/#205-todo-date-check`). `라벨`은 이슈의 **작업 유형 라벨**에 있는 영문 유형을 사용한다. 예를 들어 `🛠️ fix`는 `fix`, `⚙️ setting`은 `setting`이다. 담당자 등 다른 라벨이나 이모지는 브랜치명에 넣지 않는다. 작업 유형 라벨이 없거나 여러 개면 이슈 제목의 `[type]`과 대조해 하나로 정한다.
- `내용`은 작업을 구분할 핵심 영문 키워드 1~4개를 소문자·하이픈으로 연결하고 **30자 이내**로 쓴다. 이슈 제목의 긴 문장을 그대로 복사하지 않고 공백·문장부호를 넣지 않는다. 예: `google-login`, `todo-date-check`.
- 이슈 번호를 실제 생성 결과로 확인한 뒤 GitHub 이슈의 **Development → Create a branch** 또는 `gh issue develop <번호> --base develop --name '<라벨>/#<번호>-<내용>' --checkout`으로 이슈 연결 브랜치를 만들고 로컬에서 해당 브랜치로 전환한다. 기존 연결 브랜치가 있으면 먼저 확인해 재사용한다. checkout 후 현재 브랜치가 의도한 이름인지 확인한다. 작업 중인 브랜치나 미커밋 변경을 형식 때문에 임의로 이동하지 않는다.

## 커밋

- 형식: `[#<issue-number>] <type>(<scope>): <한국어 요약>`. 여러 클래스에 걸치거나 scope가 의미 없으면 괄호를 생략한다. 예: `[#205] chore: Git 작업 지침과 스킬 추가`.
- 주요 타입은 `feat`, `fix`, `refactor`, `chore`, `docs`, `test`다. 필요한 경우 브랜치 타입인 `style`, `remove`, `rename`, `comment`, `hotfix`, `setting`도 사용한다.
- 한 커밋은 하나의 검토 가능한 변경 이유를 담는다. 같은 기능에서도 의존성·설정, 도메인·영속성, 서비스, API·DTO·OpenAPI, 문서가 분리 가능하면 각각 별도 커밋으로 계획한다. 결합된 변경을 억지로 분리해 중간 커밋의 빌드를 깨지는 않는다. `git add .`로 무관한 변경을 포함하지 않는다.
- “커밋해줘”는 파일·hunk·메시지를 나눈 계획을 제시하는 **Phase 1**이다. “커밋 계획 승인”이 다음 요청으로 왔고 diff가 그대로일 때만 필요한 unstaged·새 파일을 선별해 스테이징하고 **Phase 2**에서 커밋한다. PR 요청은 커밋 승인이 아니다.
- 인증서·개인키나 실제 비밀번호·API 키·토큰이 커밋 후보에 있으면 스테이징·커밋을 멈추고 값을 노출하지 않는다. `--no-verify`로 검증을 건너뛰지 않는다.
- 커밋 전 `git diff --check`와 변경 유형에 맞는 검증을 수행한다. Java 변경은 `./gradlew build`로 확인하고 실패를 성공으로 보고하지 않는다.

## PR과 리뷰

- 제목: `[<type>] #<issue-number> - <한국어 요약>` (예: `[setting] #205 - Git 작업 자동화`). 기본 대상은 `develop`; `deploy` PR은 별도 운영 절차다.
- PR 생성은 별도의 “PR 만들어줘” 명령에서만 한다. 이 단계에서 미커밋 변경을 커밋하거나 미푸시 커밋을 푸시하지 않는다. 먼저 “푸시해줘”로 원격 반영을 마친 브랜치만 제출한다.
- 본문은 [PR 템플릿](../../.github/PULL_REQUEST_TEMPLATE)에 따라 실제 변경·검증·위험·미완료 작업·리뷰 요청을 적는다. 같은 head 브랜치의 열린 PR이 있으면 재사용한다. 구현과 로컬 검증이 끝난 제출 요청은 리뷰 가능한 PR로 만들고, 미완료 작업을 공유하거나 사용자가 Draft를 요청했을 때만 Draft로 만든다. 현재 `.coderabbit.yaml`은 Draft 자동 리뷰를 끄고 있으므로 Draft에서 리뷰를 기대하지 않는다.
- `closes #번호`의 자동 이슈 종료는 PR 대상이 GitHub 기본 브랜치인 경우에만 기대한다. PR 작성 시 기본 브랜치를 확인한다.
- `develop`·`deploy` 대상 PR의 Java 17 Gradle 빌드 확인은 [PR checks](../../.github/workflows/pr-check.yml)가 수행한다. CI 성공과 사람 2명의 코드 리뷰 완료 후 머지한다. GitHub Ruleset에서 필수 리뷰·상태 검사를 설정해야 실제로 강제된다.
- 리뷰에서는 존댓말을 사용하고, 받은 리뷰를 확인했으면 반응을 남긴다. CodeRabbit 의견은 참고하되 사람의 판단을 대체하지 않는다. 서버 톡방 공지는 담당자가 하고, 에이전트는 명시적 전송 요청 없이 메시지를 보내지 않는다.

실행 절차: [timo-issue](../../.agents/skills/git/timo-issue/SKILL.md) · [timo-commit](../../.agents/skills/git/timo-commit/SKILL.md) · [timo-push](../../.agents/skills/git/timo-push/SKILL.md) · [timo-pr](../../.agents/skills/git/timo-pr/SKILL.md).
