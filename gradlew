#!/usr/bin/env sh
set -eu

DIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
JAR_DIR="$DIR/gradle/wrapper"
CLASSPATH="$JAR_DIR/gradle-wrapper.jar:$JAR_DIR/gradle-wrapper-shared-8.9.jar"

exec java -cp "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
