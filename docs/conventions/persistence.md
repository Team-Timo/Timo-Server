# JPA와 스키마 변경

Entity·Repository·트랜잭션 또는 테이블·컬럼을 바꿀 때 읽는다.

## 현재 영속성 구조

- `domain/{기능}/entity`의 Java `@Entity`와 같은 도메인의 Spring Data `JpaRepository`가 기본이다. 별도 `infrastructure:db` 모듈이나 `{Domain}JpaEntity → toDomain()` 변환을 의무화하지 않는다.
- 테이블·컬럼은 `@Table`, `@Column`, 관계는 `@JoinColumn` 등으로 의도를 명시한다. 기존 Entity는 `@ManyToOne(fetch = LAZY)` 연관관계도 사용하므로 다른 aggregate를 반드시 ID 필드로만 저장하라는 규칙을 가져오지 않는다.
- 생성·수정 시각은 `global/common/BaseTimeEntity`의 Auditing을 사용한다. Entity의 기본 필드·일반 정보·관계를 읽기 좋게 묶고, 변경 메서드가 도메인 불변식을 유지하도록 한다.
- 조회는 기존 Repository의 메서드·JPQL·projection을 먼저 살핀다. 사용자별 데이터에는 소유권 조건을 빠뜨리지 않고, 목록 조회에서는 연관관계 로딩과 N+1 가능성을 확인한다.
- 변경 작업은 Service의 트랜잭션 안에서 수행한다. 읽기 전용 조회는 기존 `@Transactional(readOnly = true)` 패턴을 따른다.

## 스키마 변경 시 확인

1. 기존 데이터·NULL 값·기본값과 이전 애플리케이션 버전의 호환성을 확인한다. 테이블·컬럼 삭제나 타입 변경은 운영 데이터 영향과 복구 방법까지 검토한다.
2. Entity 매핑뿐 아니라 배포 순서, 필요한 데이터 보정, 관련 조회·응답 변경을 함께 검토한다. 조회에 필요한 인덱스와 잠금 영향도 확인한다.
3. 현재 `application.yml`과 `application-prod.yml`에는 `spring.jpa.hibernate.ddl-auto: update`가 있고, 프로젝트에 Flyway 의존성·마이그레이션 디렉터리는 없다. 참고 저장소의 `V{버전}__...sql` 파일이나 `ddl-auto: validate`를 현재의 의무 규칙으로 적지 않는다. 운영 마이그레이션 체계 전환은 별도 설계·배포 작업으로 다룬다.

참고 구현: `domain/todo/entity/Todo.java`, `domain/todo/repository/TodoRepository.java`, `global/common/BaseTimeEntity.java`.
