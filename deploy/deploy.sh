#!/bin/bash
set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

cd ~/timo-deploy

# Compose 파일 및 nginx 설정 경로
COMPOSE_FILE="docker-compose.prod.yml"
DOCKER_COMPOSE="sudo docker compose -f $COMPOSE_FILE"
NGINX_CONF="/etc/nginx/sites-available/timo-api"

# 옵션 파싱
KEEP_OLD=false
while [[ "$#" -gt 0 ]]; do
    case $1 in
        --keep-old) KEEP_OLD=true ;;
        *) log_error "Unknown parameter: $1"; exit 1 ;;
    esac
    shift
done

###############################################################################
# 1. Redis 기동
###############################################################################

log_info "Redis 상태 확인/기동..."
$DOCKER_COMPOSE up -d redis

###############################################################################
# 2. 현재 활성 환경 확인
###############################################################################

if [ ! -f "$NGINX_CONF" ]; then
    log_error "Nginx 설정 파일을 찾을 수 없음: $NGINX_CONF"
    exit 1
fi

ACTIVE_LINE=$(grep "ACTIVE_CONTAINER" "$NGINX_CONF" || echo "")

if echo "$ACTIVE_LINE" | grep -q "localhost:8080"; then
    ACTIVE="blue"
    INACTIVE="green"
    INACTIVE_PORT=8081
elif echo "$ACTIVE_LINE" | grep -q "localhost:8081"; then
    ACTIVE="green"
    INACTIVE="blue"
    INACTIVE_PORT=8080
else
    log_error "활성 환경 판별 실패. nginx.conf의 ACTIVE_CONTAINER 마킹 확인 필요."
    exit 1
fi

log_info "======================================"
log_info " 현재 활성: ${YELLOW}$ACTIVE${NC}"
log_info " 배포 대상: ${YELLOW}$INACTIVE${NC} (포트: $INACTIVE_PORT)"
log_info "======================================"

###############################################################################
# 3. 최신 이미지 pull
###############################################################################

log_info "[1/5] 최신 이미지 pull..."
$DOCKER_COMPOSE pull app-$INACTIVE

###############################################################################
# 4. 비활성 컨테이너 재생성
###############################################################################

log_info "[2/5] app-$INACTIVE 컨테이너 재생성..."
$DOCKER_COMPOSE stop app-$INACTIVE || true
$DOCKER_COMPOSE rm -f app-$INACTIVE || true
$DOCKER_COMPOSE up -d app-$INACTIVE

###############################################################################
# 5. Health Check
###############################################################################

log_info "[3/5] Health check 대기 중..."

HEALTH_CHECK_TIMEOUT=180
HEALTH_CHECK_INTERVAL=5
ELAPSED=0
CONTAINER_NAME="timo-app-$INACTIVE"

while [ $ELAPSED -lt $HEALTH_CHECK_TIMEOUT ]; do
    HEALTH_STATUS=$(sudo docker inspect --format='{{.State.Health.Status}}' "$CONTAINER_NAME" 2>/dev/null || echo "unknown")

    if [ "$HEALTH_STATUS" == "healthy" ]; then
        log_success "$CONTAINER_NAME healthy (${ELAPSED}s)"
        break
    fi

    log_info "상태: $HEALTH_STATUS (${ELAPSED}s / ${HEALTH_CHECK_TIMEOUT}s)"
    sleep $HEALTH_CHECK_INTERVAL
    ELAPSED=$((ELAPSED + HEALTH_CHECK_INTERVAL))
done

if [ "$HEALTH_STATUS" != "healthy" ]; then
    log_error "$CONTAINER_NAME 헬스체크 실패. 배포 중단."
    log_error "로그 확인: sudo docker logs $CONTAINER_NAME --tail 100"
    $DOCKER_COMPOSE stop app-$INACTIVE
    exit 1
fi

###############################################################################
# 6. Nginx 트래픽 전환
###############################################################################

log_warning "[4/5] Nginx 트래픽 전환: $ACTIVE -> $INACTIVE"

if [ "$INACTIVE" == "green" ]; then
    sudo sed -i.bak "s|server localhost:8080;  # ACTIVE_CONTAINER|server localhost:8081;  # ACTIVE_CONTAINER|" "$NGINX_CONF"
else
    sudo sed -i.bak "s|server localhost:8081;  # ACTIVE_CONTAINER|server localhost:8080;  # ACTIVE_CONTAINER|" "$NGINX_CONF"
fi

log_info "Nginx 설정 문법 확인..."
if ! sudo nginx -t; then
    log_error "Nginx 설정 오류! 백업으로 복구합니다."
    sudo mv "$NGINX_CONF.bak" "$NGINX_CONF"
    $DOCKER_COMPOSE stop app-$INACTIVE
    exit 1
fi

log_info "Nginx reload..."
sudo systemctl reload nginx

log_success "트래픽 전환 완료: $INACTIVE"

###############################################################################
# 7. 이전 컨테이너 정리
###############################################################################

if [ "$KEEP_OLD" == true ]; then
    log_warning "[5/5] 이전 버전($ACTIVE) 유지 (--keep-old)"
else
    log_info "[5/5] 10초 후 이전 버전($ACTIVE) 정지..."
    sleep 10
    $DOCKER_COMPOSE stop app-$ACTIVE
    log_success "이전 버전($ACTIVE) 정지 완료"
fi

sudo docker image prune -f > /dev/null

log_success "======================================"
log_success " Blue-Green 배포 완료"
log_success " 활성: $INACTIVE"
log_success "======================================"
