---
name: timo-pr
description: Timo Server에서 "PR 만들어줘", "Draft PR 올려줘", "PR 갱신해줘"처럼 PR 단계를 명시적으로 요청할 때만 사용한다. 커밋·푸시는 하지 않는다.
---

# Timo PR

## 트리거와 단계 경계

- **"PR 만들어줘", "PR 올려줘"**: 이미 푸시된 작업 브랜치에서 `develop` 대상 PR을 생성하거나 기존 PR을 갱신한다.
- **"Draft PR 올려줘"**: 위 조건을 확인한 뒤 Draft 상태로 제출한다.
- **"PR 갱신해줘"**: 같은 head 브랜치의 기존 PR 제목·본문을 실제 diff에 맞게 수정한다.
- **"PR 본문 초안 써줘"**: 로컬에서 제목·본문만 작성한다. GitHub PR은 생성·수정하지 않는다.
- 구현·커밋·푸시 요청만으로 PR을 만들지 않는다. 이 스킬은 `git add`, `git commit`, `git push`를 실행하지 않는다.

## 실행

1. [Git 규칙](../../../../docs/conventions/git-convention.md)과 `.github/PULL_REQUEST_TEMPLATE`을 확인한다. `git status --short --branch`, `origin/develop...HEAD`의 커밋·변경 파일, 이슈 번호, PR head/base를 확인한다. `develop`·`main`·`deploy`에서 작업 브랜치 PR을 제출하지 않는다.
2. 제출 요청이라면 현재 로컬 커밋이 원격 작업 브랜치에 모두 반영됐는지 확인한다. 미커밋 작업 변경, 미푸시 커밋, 원격 브랜치 부재가 있으면 PR 본문 초안과 다음에 필요한 **커밋 또는 푸시 명령**을 알려주고 멈춘다. 대신 커밋하거나 푸시하지 않는다.
3. 제목은 `[type] #번호 - 한국어 요약`으로 하고, 템플릿 본문에 실제 변경 이유·주요 내용·검증 결과·위험·미완료 작업·리뷰 요청을 적는다. GitHub 기본 브랜치가 `develop`일 때만 `closes #번호`의 자동 종료를 기대한다. 그렇지 않으면 이슈를 참조하거나 수동 연결한다.
4. 같은 원격 head 브랜치의 열린 PR을 먼저 조회한다. 있으면 필요한 제목·본문만 갱신하고, 없으면 `--base develop --head <브랜치> --title ... --body-file ...`로 생성한다. 완료된 변경은 리뷰 가능한 PR로, 명시적인 Draft 요청이나 미완료 공유는 `--draft`로 제출한다.
5. PR URL과 CI 결과 또는 진행 중 상태를 확인해 보고한다. `.coderabbit.yaml`은 `develop` 대상의 Draft가 아닌 PR만 자동 리뷰하며 Markdown 변경은 제외한다. CI 실패나 리뷰 수정 요청이 있으면 다음 구현 단계에서 다루고, 이 PR 단계에서 코드 수정·커밋·푸시를 연쇄 실행하지 않는다. 사람의 리뷰·승인, 머지, `deploy` 배포, 서버 톡방 전송도 자동으로 진행하지 않는다.
