# 알림 대응 Runbook

Discord로 알림을 받았을 때 확인할 순서를 정리한다. 알림 구성은 [README.md](README.md), 규칙별 기준은 [alert-rules.md](alert-rules.md)를 본다.

## 공통 순서

1. 알림 메시지의 규칙 이름과 요약으로 어떤 알림인지 확인한다.
2. 이미 누군가 대응 중인지 채널에서 확인하고, 대응을 시작하면 채널에 남긴다.
3. 아래 알림별 순서로 원인을 확인한다. Grafana는 [README.md](README.md)의 SSH 터널로 접속한다.
4. 복구(Resolved) 알림이 오는지 확인한다. 오지 않으면 원인이 남아 있는 것이다.
5. 원인을 코드나 설정에서 고쳐야 하면 이슈를 만들어 기록한다.

서버 확인에 쓰는 이름은 아래와 같다. 서버(EC2)에서 `~/timo-deploy`로 이동해 실행한다.

| 대상 | 컨테이너 이름 |
| --- | --- |
| 앱 | `timo-app-blue`, `timo-app-green` |
| Prometheus / Grafana | `timo-prometheus`, `timo-grafana` |
| Redis | `timo-redis` |

```bash
sudo docker ps -a
sudo docker logs timo-app-blue --tail 100
```

## 알림별 확인 순서

### 서버 다운

활성 인스턴스가 0개이거나 지표 수집이 실패한 상태다.

1. Prometheus 자체가 죽은 경우에도 이 알림이 올 수 있다. 먼저 `sudo docker ps -a`로 `timo-prometheus`가 실행 중인지 확인한다. Prometheus가 문제이고 앱이 정상이면 서비스 장애가 아니다.
2. `timo-app-blue`, `timo-app-green` 중 실행 중인 컨테이너가 있는지 확인한다.
3. 앱이 떠 있으면 서버에서 헬스체크를 확인한다. blue는 `curl -f http://localhost:8080/actuator/health`, green은 `curl -f http://localhost:8081/actuator/health`다.
4. 컨테이너가 꺼졌거나 비정상이면 `sudo docker logs <컨테이너> --tail 100`으로 종료 원인을 확인한다.
5. 최근 배포 직후였다면 GitHub Actions의 배포 워크플로 결과를 확인한다.
6. 서비스가 실제로 응답하는지 클라이언트 쪽에서도 확인한다.

### 5xx 급증

5분간 5xx 응답이 5건을 넘은 상태다.

1. Grafana Explore에서 어떤 요청에서 나는지 확인한다.
   ```
   sum by (uri, status) (increase(http_server_requests_seconds_count{job="timo-server", status=~"5.."}[30m]))
   ```
2. 특정 `uri`에 몰려 있는지, 전체에서 나는지 구분한다.
3. Sentry에서 같은 시간대의 에러를 확인해 예외 원인과 스택을 본다.
4. 로그는 서버에서 `sudo docker logs <컨테이너> --since 30m`으로 확인하고, 요청별 `traceId`로 추적한다.
5. 직전 배포와 시간이 겹치면 배포 변경 사항을 우선 의심한다.
6. 외부 API(공휴일 API, OAuth 등) 실패나 DB 문제가 원인인지 확인한다.

### DB 커넥션 풀 부족

HikariCP 사용률이 0.8을 넘은 상태다. 풀 최대 크기는 10이다.

1. Explore에서 사용 중 연결과 대기 요청을 확인한다.
   ```
   hikaricp_connections_active{job="timo-server"}
   ```
   ```
   hikaricp_connections_pending{job="timo-server"}
   ```
2. 대기 요청(`pending`)이 0보다 크면 이미 풀이 부족해 요청이 기다리는 상태라 더 급하다.
3. 같은 시간의 응답 지연이나 5xx가 있는지 확인한다.
4. 오래 걸리는 쿼리나 트랜잭션, 요청 폭증이 원인인지 로그와 DB에서 확인한다.
5. 트래픽 증가가 원인이면 풀 크기와 쿼리를 이슈로 만들어 검토한다.

### JVM 힙 사용률 높음

GC를 한 뒤에도 힙 사용률이 0.85를 5분 넘게 유지한 상태다.

1. Explore에서 최근 며칠간의 추이를 확인한다.
   ```
   max(jvm_memory_usage_after_gc{job="timo-server", area="heap"})
   ```
2. 배포로 재시작한 뒤 천천히 계속 오르는 모양이면 메모리가 해제되지 않는 문제를 의심한다.
3. 요청이 급증한 시점과 겹치는지, 같은 시간에 5xx나 응답 지연이 있는지 확인한다.
4. 서버 재시작은 사용률을 낮추는 임시 조치다. 재시작했더라도 원인 분석 이슈를 남긴다.
5. `Heap Used` 등은 Grafana 대시보드 `Spring Boot 3.x Statistics`, `JVM (Micrometer)`에서 볼 수 있다.

## 알림 일시 중지(Silence)

점검이나 배포 중에 알림이 반복되거나 오탐이 계속될 때 사용한다. 규칙을 지우지 않고 알림만 멈춘다.

1. Grafana에서 `Alerting → Silences → + Add Silence`를 누른다.
2. 기간을 정한다. 필요한 만큼만 짧게 잡는다.
3. 대상 규칙을 라벨 조건으로 지정한다. 예: `alertname = 5xx 급증`. 전체를 멈추려면 `env = prod`를 사용한다.
4. 코멘트에 이유와 작성자를 적고 저장한다.
5. 기간이 끝나면 알림이 다시 전송된다. 조기 해제는 Silences 목록에서 만료시킨다.

Silence 중에는 장애도 알림으로 오지 않는다. 서버 다운 알림은 가능하면 멈추지 않고, 멈췄다면 채널에 남긴다.

## 정식 오픈 후 점검 체크리스트

현재 임계치는 개발자 테스트 트래픽 기준이다. 정식 오픈 후 1주일 정도 지표를 보고 아래를 점검한다.

- [ ] 5xx 알림: 5분당 요청 수와 5xx 건수를 보고 건수 기준(5건)이 적절한지 확인한다. 요청이 늘면 비율 기준(예: 5%)과 최소 요청 수 조건으로 바꾸는 것을 검토한다.
- [ ] 응답 지연 알림(현재 없음): 요청이 늘어 지표가 안정되면 p95 응답 시간 평소 값을 확인하고 알림 추가를 검토한다.
- [ ] DB 커넥션 풀: 실제 사용량 최대값을 보고 임계치 0.8과 풀 크기 10이 적절한지 확인한다.
- [ ] JVM 힙: 평소 사용률과 증가 추세를 확인하고 0.85와 지속 시간 5분이 적절한지 확인한다.
- [ ] 평소 지표 기준 표를 실제 오픈 후 값으로 갱신한다([alert-rules.md](alert-rules.md)).
- [ ] 오탐(불필요한 알림)과 누락(장애인데 알림이 없던 경우)이 있었는지 돌아본다.
- [ ] 규칙과 Contact Point를 다시 Export해 백업한다.
- [ ] 변경한 값은 문서와 [README.md](README.md)의 변경 이력에 반영한다.
