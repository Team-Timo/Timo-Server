# Timo Server 모니터링 알림

Prometheus가 수집한 서버 메트릭을 Grafana Alerting으로 평가하고, 임계치를 넘으면 Discord로 알린다.
알림 구성을 이해하거나 Grafana에 접속할 때 읽는다.
규칙별 기준은 [alert-rules.md](alert-rules.md), 알림을 받은 뒤의 대응은 [runbook.md](runbook.md)를 본다.

## 구성 범위

- 이 문서는 서버 메트릭(서버 상태, 요청, DB 커넥션 풀, JVM) 알림을 다룬다.
- 예상하지 못한 예외·에러 알림은 Sentry가 Slack으로 보내며 이 문서의 대상이 아니다.

## 알림 전송 경로

1. `deploy/prometheus/prometheus.yml`이 `timo-server`의 `/actuator/prometheus`를 15초마다 수집한다. blue·green 두 컨테이너를 모두 수집한다.
2. Grafana의 알림 규칙이 Prometheus 데이터를 1분마다 평가한다. 조건이 규칙별 지속 시간(1~5분) 동안 유지되면 알림이 발생(Firing)하고, 조건이 풀리면 복구(Resolved)된다. 규칙별 지속 시간은 [alert-rules.md](alert-rules.md)에 있다.
3. 알림 정책이 알림을 Contact Point로 보낸다. Contact Point `monitoring-alert`에는 Discord 연동만 있다.
4. 메시지 본문은 Grafana 알림 템플릿(`timo.message`)으로 만든다. 요약, 설명, 환경, 상태와 Grafana 링크(규칙 보기·알림 일시 중지·대시보드·패널)를 담는다.

Prometheus와 Grafana는 `deploy/docker-compose.prod.yml`로 운영 서버(EC2)에서 실행한다.

## 알림 관리 방식

알림 규칙·Contact Point·알림 정책·메시지 템플릿은 **Grafana 화면에서 직접 구성**한다. 저장소의 프로비저닝 파일로 관리하지 않는다.

- 장점: PR·배포·재시작 없이 임계치를 바로 조정할 수 있다.
- 한계: 설정이 Grafana 데이터 볼륨(`grafana-data`)에만 있어 변경 이력이 Git에 남지 않고, 볼륨이 사라지면 설정도 사라진다.
- 그러므로 규칙·임계치·채널 설정을 이 문서 폴더에 기록하고, 설정을 바꾸면 문서도 같은 PR로 갱신한다.
- 화면에서 규칙과 Contact Point를 Export한 yml을 서버 공유 공간에 백업해 둔다. Webhook URL은 지우고 보관하며 저장소에 올리지 않는다.

## Grafana 접속

Grafana 컨테이너는 서버의 `127.0.0.1:3000`에만 열려 있어 외부에서 바로 접속할 수 없다. SSH 포트 포워딩으로 접속한다.

```bash
ssh -i <키 파일 경로> -L 3000:localhost:3000 ubuntu@<EC2_HOST>
```

- 터널을 연 상태에서 브라우저로 `http://localhost:3000`에 접속한다.
- 로그인 계정은 서버 `~/timo-deploy/.env`의 `GRAFANA_ADMIN_USER`·`GRAFANA_ADMIN_PASSWORD` 값이다.
- 접속 권한(EC2 SSH 키, Grafana 계정)은 담당자에게 요청한다.

## 비밀값 관리

- Discord Webhook URL은 Grafana의 Contact Point에만 입력한다. 화면에서 다시 확인할 수 없는 보안 설정으로 저장된다.
- Webhook URL, Grafana 계정 값, SSH 키를 저장소·문서·PR·채팅·이슈에 적지 않는다.
- Webhook URL이 노출되면 Discord 채널 설정에서 해당 Webhook을 삭제·재발급하고 Contact Point의 URL을 교체한다.
- 알림 메시지에는 토큰·쿠키·요청 본문·개인정보를 넣지 않는다. 규칙의 Summary·Description에도 넣지 않는다.

## 알려진 한계

- **알림 링크:** Grafana에 외부 접속 주소(`root_url`)가 없어 링크가 `localhost:3000`으로 만들어진다. Grafana를 인터넷에 공개하지 않으려고 이 상태로 두었고, 터널이 열려 있는 컴퓨터에서만 링크가 열린다. Grafana를 외부에 공개하려면 `GF_SERVER_ROOT_URL`과 nginx 프록시, 접근 제한을 별도 작업으로 진행한다.
- **설정이 Git에 없음:** 위 관리 방식의 한계와 같다. 변경은 이 문서와 변경 이력에 남긴다.
- **오픈 전 트래픽 기준:** 임계치는 개발자 테스트 트래픽으로 정했다. 정식 오픈 후 지표를 보고 다시 조정한다.
- **응답 지연 알림 없음:** 서버가 응답 시간 히스토그램 지표(`http_server_requests_seconds_bucket`)를 내보내지 않아 p95 응답 시간을 계산할 수 없고, 요청이 적어 지표도 불안정해서 만들지 않았다. 필요하면 서버에서 해당 지표를 켜는 설정 변경부터 별도 작업으로 진행한다.
- **기존 `AllInstancesDown`:** `deploy/prometheus/rules/alerts.yml`의 규칙은 알림 전송 대상이 없다. 같은 조건의 서버 다운 알림은 Grafana 규칙이 담당한다.

## 변경 이력

설정을 바꾸면 한 줄을 추가한다.

| 날짜 | 변경 | 이슈 |
| --- | --- | --- |
| 2026-11-02 | Grafana 알림 초기 구성(Slack·Discord Contact Point, 규칙 4개, 메시지 템플릿) | #244 |
| 2026-11-04 | Slack 연동 제거(Discord만 사용) | #244 |
| 2026-11-04 | 메시지 템플릿에 Grafana 링크를 한글 이름으로 추가 | #244 |
| 2026-11-04 | JVM 힙 알림을 GC 직후 사용률 지표(`jvm_memory_usage_after_gc`)로 변경 | #244 |
