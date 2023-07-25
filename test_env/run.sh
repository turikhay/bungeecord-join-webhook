#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

SELF=$(SELF=$(dirname "$0") && bash -c "cd \"$SELF\" && pwd")

ACTION="$1"

PROXY_TYPE="bungeecord"
VERSION="1.20.1"
JAVA_VERSION=17

SERVER_VERSION="$VERSION"
JAR="$SELF/../build/libs/BungeeCordJoinWebhook.jar"
NAME="bjw_test_"$PROXY_TYPE"_"$VERSION
TEST_CONTAINER_NAME="$NAME"
TEST_ENV="$SELF/test_env/$NAME"
PLUGINS_SERVER="$TEST_ENV/plugins_server"
PLUGINS_PROXY="$TEST_ENV/plugins_proxy"
VERSION_INFO_FILE_NAME="docker-compose.version_info.yml"
VERSION_INFO_FILE="$TEST_ENV/$VERSION_INFO_FILE_NAME"
OVERRIDE_FILE_NAME="docker-compose.override.yml"
OVERRIDE_FILE="$TEST_ENV/$OVERRIDE_FILE_NAME"

set +u
if [[ -z $DEBUG ]]; then
  DEBUG=""
  if [[ $ACTIONS_STEP_DEBUG == "true" ]]; then
    DEBUG=1
  fi
fi
if [[ $DEBUG ]] || [[ $GRADLE_REBUILD ]]; then
  (cd .. && ./gradlew build)
fi
set -u

function debug_echo {
  if [[ $DEBUG ]]; then
    echo $@
  fi
}

debug_echo "Working in: $TEST_ENV"
rm -rf "$TEST_ENV"
mkdir -p "$TEST_ENV"

debug_echo "Populating server plugins directory"
rm -rf "$PLUGINS_SERVER"
mkdir "$PLUGINS_SERVER"
# cp -r "$JAR" "$PLUGINS_SERVER/"
cp -r "$SELF/server/plugins/"* "$PLUGINS_SERVER/"

debug_echo "Populating proxy plugins directory"
rm -rf "$PLUGINS_PROXY"
mkdir -p "$PLUGINS_PROXY"
cp -r "$JAR" "$PLUGINS_PROXY/"
cp -r "$SELF/proxy/$PROXY_TYPE/plugins/"* "$PLUGINS_PROXY/"

debug_echo "Writing $VERSION_INFO_FILE_NAME"

cat << EOF > "$VERSION_INFO_FILE"
services:
  server:
    build:
      args:
        - TAG=java$JAVA_VERSION
      tags:
        - "bjw-test-server:java$JAVA_VERSION"
    environment:
      - VERSION=$SERVER_VERSION

EOF

debug_echo "Writing $OVERRIDE_FILE_NAME"

cat << EOF > "$OVERRIDE_FILE"
services:
  proxy:
    volumes:
      - $TEST_ENV/plugins_proxy:/server/plugins
  server:
    volumes:
      - $TEST_ENV/plugins_server:/data/plugins
EOF

FILES=(
    "$SELF/docker-compose.yml"
    "$SELF/docker-compose.$PROXY_TYPE.yml"
    "$VERSION_INFO_FILE"
    "$OVERRIDE_FILE"
)

FILES_FMT=`printf ' -f %s' "${FILES[@]}"`

function docker_compose {
  bash -c "docker compose$FILES_FMT $@"
}

function docker_compose_down() {
  docker_compose down --rmi local
}

function perform_test {
  debug_echo "Stopping old containers"
  docker_compose_down >/dev/null

  debug_echo "Starting container"
  docker_compose "up --force-recreate --build"
}

case $ACTION in
  down)
    echo "[ACTION] Down"
    docker_compose down
    ;;

  convert)
    echo "[ACTION] Convert"
    docker_compose convert
    ;;

  build)
    echo "[ACTION] Building"
    docker_compose build
    ;;

  manual)
    echo "[ACTION] Spinning up the server for manual test; version: $VERSION, proxy: $PROXY_TYPE"
    perform_test "manual"
    ;;

  cleanup)
    echo "[ACTION] Cleaning up"
    docker_compose "down --remove-orphans --rmi all -v"
    ;;

  *)
    echo "Unknown action"
    exit 1
    ;;
esac
