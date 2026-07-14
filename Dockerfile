# ==================== Stage 1: 빌드 ====================
FROM eclipse-temurin:17-jdk AS builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 파일들 먼저 복사 (레이어 캐싱 최적화)
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./

# gradlew 실행 권한 부여
RUN chmod +x ./gradlew

# 의존성 먼저 다운로드 (캐싱)
RUN ./gradlew dependencies --no-daemon

# 소스 코드 복사
COPY src src

# JAR 빌드 (테스트 제외)
RUN ./gradlew bootJar -x test --no-daemon


# ==================== Stage 2: 실행 ====================
FROM eclipse-temurin:17-jre

# curl 설치 (Docker healthcheck용)
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

# 실행 포트
EXPOSE 8080

# JVM 옵션 설정 (t3.micro 1GB RAM 고려)
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"

# 실행 명령
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
