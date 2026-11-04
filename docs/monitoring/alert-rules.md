# 알림 규칙

Grafana에 구성한 알림 규칙의 쿼리·임계치·지속 시간·메시지와 그 근거를 기록한다. 임계치를 바꾸거나 Grafana 설정을 다시 만들 때 읽는다. 구성과 접속 방법은 [README.md](README.md)를 본다.

규칙을 바꾸면 이 문서와 [README.md](README.md)의 변경 이력을 같은 PR로 갱신한다.

## 공통 설정

| 항목 | 값                                        |
| --- |------------------------------------------|
| 데이터 소스 | Prometheus                               |
| 폴더 / 평가 그룹 | `timo` / `timo-app`                      |
| 평가 주기 | 1분                                       |
| 라벨 | `env = prod`                             |
| Contact Point | `monitoring-alert` (Discord)             |
| 알림 조건 | Reduce(`B`, Last)한 값에 Threshold(`C`)를 적용 |

- 지속 시간(Pending period)은 조건이 이 시간 동안 계속되어야 알림을 보내는 오탐 방지 설정이다. 규칙별로 다르다.
- No data 처리는 쿼리 결과가 비어 있을 때의 상태다. 장애 신호가 없어서 데이터가 비는 규칙은 `NORMAL`, 데이터가 비는 것 자체가 장애인 규칙은 `Alerting`으로 둔다.
- 실행 오류 처리는 쿼리 실행이 실패하거나 시간 초과가 났을 때의 상태다. Prometheus에 연결하지 못하면 알림 판단 자체가 불가능하므로, 서버 다운 규칙은 `Alerting`으로 두어 알림을 보내고 나머지 규칙은 `Error` 상태로 표시한다.

## 규칙 목록

| 규칙 | 조건 | 지속 시간 | No data  | 실행 오류 |
| --- | --- | --- |----------| --- |
| 서버 다운 | 활성 인스턴스 수 1 미만 | 1분 | Alerting | Alerting |
| 5xx 급증 | 5분간 5xx 5건 초과 | 1분 | NORMAL   | Error |
| DB 커넥션 풀 부족 | 풀 사용률 0.8 초과 | 2분 | NORMAL   | Error |
| JVM 힙 사용률 높음 | GC 직후 힙 사용률 0.85 초과 | 5분 | NORMAL   | Error |

### 1. 서버 다운

- 쿼리: `sum(up{job="timo-server"})`
- 조건: Threshold `IS BELOW 1`
- Summary: `서버 다운: 활성 인스턴스 0개`
- Description: `blue/green 모두 수집 실패 (1분 이상)`
- 근거: blue·green 중 한쪽만 켜져 있는 것은 정상이다. 개별 인스턴스가 아니라 살아있는 인스턴스가 0개일 때만 알린다. 지속 시간 1분은 기존 `deploy/prometheus/rules/alerts.yml`의 `AllInstancesDown`(`for: 1m`)과 맞췄다. 수집이 아예 실패하는 상황도 장애로 보아 No data를 `Alerting`으로 둔다.

### 2. 5xx 급증

- 쿼리: `sum(increase(http_server_requests_seconds_count{job="timo-server", status=~"5.."}[5m]))`
- 조건: Threshold `IS ABOVE 5`
- Summary: `5xx 급증: 5분간 {{ $values.B.Value }}건`
- Description: `5분간 5xx 5건 초과 (평소 최대 약 4건)`
- 근거: 요청이 적은 지금은 비율(%)이 의미가 없어 발생 건수로 판단한다. 5xx가 없으면 지표가 비므로 No data를 `NORMAL`로 둔다.

### 3. DB 커넥션 풀 부족

- 쿼리: `max(hikaricp_connections_active{job="timo-server"} / hikaricp_connections_max{job="timo-server"})`
- 조건: Threshold `IS ABOVE 0.8`
- Summary: `DB 풀 사용률 높음: {{ humanizePercentage $values.B.Value }}`
- Description: `HikariCP 사용률 80% 초과 (최대 10개, 2분 이상 지속)`
- 근거: 풀 최대 크기는 10이고 평소 사용량은 거의 0이다. blue·green 중 높은 쪽 값으로 평가한다. 순간적인 증가는 지속 시간 2분으로 거른다.

### 4. JVM 힙 사용률 높음

- 쿼리: `max(jvm_memory_usage_after_gc{job="timo-server", area="heap"})`
- 조건: Threshold `IS ABOVE 0.85`
- Summary: `JVM 힙 메모리 사용률 높음: {{ humanizePercentage $values.B.Value }}`
- Description: `GC 후에도 힙 사용률 85% 초과, 5분 이상 지속`
- 근거: 서버는 Serial GC를 사용한다(Dockerfile에 GC 옵션이 없어 JVM이 컨테이너 사양을 보고 자동으로 고른다). Serial GC는 old 영역이 거의 찰 때까지 기다렸다가 Full GC로 한 번에 비우기 때문에, 현재 사용량으로 판단하면 누수가 없어도 정리 직전에 85%를 넘어 머물 수 있다. 그래서 GC 직후 사용률(`jvm_memory_usage_after_gc`, 0~1)로 "정리해도 메모리가 줄지 않는지"를 판단한다. 이 지표에는 영역 이름(`id`) 라벨이 없어서 서버 사양을 올려 G1 GC 등으로 바뀌어도 쿼리를 바꾸지 않아도 된다. 메모리는 순간적으로 오르내릴 수 있어 지속 시간을 5분으로 길게 둔다.

## 평소 지표 기준

개발자 테스트 트래픽으로 확인한 값이다. 정식 오픈 전 기준이므로 오픈 후 다시 확인한다.

| 지표 | 평소 값 |
| --- | --- |
| 5xx 발생 건수(5분) | 대부분 0~1건, 간헐적으로 2~3건, 최대 약 4건 |
| 전체 요청 수(5분) | 대부분 0에 가까움, 부하 테스트 구간 최대 약 3,000건 |
| 활성 인스턴스 수 | 1 (blue·green 중 한쪽만 실행) |
| HikariCP 최대 연결 / 사용 | 최대 10개 / 사용 최대 1개(10%), 대기 요청 0 |
| GC 직후 힙 사용률 | 약 15~24%, 배포로 재시작하면 낮아짐 |

## 알림 정책

기본 정책(Default policy)을 사용한다.

| 항목 | 값 |
| --- | --- |
| Contact Point | `monitoring-alert` |
| Group wait | 30초 |
| Group interval | 5분 |
| Repeat interval | 4시간 |

복구(Resolved) 알림은 Contact Point 설정에서 끄지 않아 전송된다.

## 메시지 템플릿

알림 템플릿 `timo-message`에 `timo.message`를 정의하고, Contact Point의 Discord(Message Content) 본문에서 `{{ template "timo.message" . }}`로 사용한다.

```
{{ define "timo.message" }}
{{ range .Alerts }}
**{{ .Annotations.summary }}**
{{ .Annotations.description }}
알림: {{ .Labels.alertname }}
환경: {{ .Labels.env }}
상태: {{ if eq .Status "firing" }}발생{{ else }}복구{{ end }}

[규칙 보기]({{ .GeneratorURL }})
[알림 일시 중지]({{ .SilenceURL }}){{ if .DashboardURL }}
[대시보드]({{ .DashboardURL }}){{ end }}{{ if .PanelURL }}
[패널]({{ .PanelURL }}){{ end }}
{{ end }}
{{ end }}
```

- 링크는 Grafana가 만드는 값이라 `localhost:3000`으로 시작한다. 터널이 열려 있는 컴퓨터에서만 열린다([README.md](README.md)의 Grafana 접속).
- 대시보드·패널 링크는 규칙에 대시보드를 연결한 경우에만 나온다.

## 규칙 변경과 검증

1. 규칙을 수정하고 저장한다. 변경한 값을 이 문서에 반영한다.
2. 임계치를 임시로 낮추고 지속 시간을 `0s`로 해서 알림과 복구(Resolved) 메시지가 Discord에 오는지 확인한다.
3. 반드시 임계치와 지속 시간을 원래 값으로 되돌리고, 알림 상태가 `Normal`로 돌아왔는지 확인한다.

## Grafana 설정을 다시 만드는 순서

`grafana-data` 볼륨이 삭제되었거나 서버를 새로 구성할 때의 순서다. 위 표와 설정값을 기준으로 다시 입력한다. 백업해 둔 Export yml이 있으면 값 확인에 참고한다.

1. 서버의 Grafana 컨테이너가 실행 중인지 확인하고 SSH 터널로 접속한다.
2. `Connections → Data sources`에서 Prometheus가 연결되어 있는지 확인한다. 저장소의 `deploy/grafana/provisioning/datasources/datasource.yml`이 자동으로 등록한다.
3. Discord Webhook URL을 준비한다(Webhook을 새로 발급받는다).
4. `Alerting → Contact points`에서 `monitoring-alert`를 만들고 Discord 연동을 추가한다. **Test**로 수신을 확인한다.
5. `Notification Templates`에 `timo-message` 템플릿을 만들고, Discord 연동의 본문에 `{{ template "timo.message" . }}`를 넣는다.
6. `Notification policies`의 Default policy를 위 표대로 설정한다.
7. 폴더 `timo`와 평가 그룹 `timo-app`(평가 주기 1분)을 만든다.
8. 규칙 4개를 위 표와 설명대로 만든다. 라벨 `env = prod`를 빠뜨리지 않는다.
9. 위 「규칙 변경과 검증」으로 알림 수신을 확인한다.
