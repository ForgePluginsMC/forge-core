#!/usr/bin/env bash
# ForgeCore direct-javac build (Gradle daemon cannot run in this sandbox).
set -euo pipefail
ROOT="$HOME/workspace/forge-core"
DEPS="$HOME/workspace/.toolchains/paper-deps"
JAVAC="$HOME/workspace/.toolchains/jdk-25.0.4.1+1/bin/javac"
JAR="$HOME/workspace/.toolchains/jdk-25.0.4.1+1/bin/jar"
CP=$(ls "$DEPS"/*.jar | tr '\n' ':')

rm -rf "$ROOT/build" && mkdir -p "$ROOT/build/classes" "$ROOT/build/stage"
find "$ROOT/src/main/java" -name '*.java' > "$ROOT/build/sources.txt"
$JAVAC -Werror -Xlint:deprecation -parameters -d "$ROOT/build/classes" -cp "$CP" @"$ROOT/build/sources.txt"
cp -r "$ROOT/build/classes"/. "$ROOT/build/stage"/
cp "$ROOT/src/main/resources/plugin.yml" "$ROOT/src/main/resources/config.yml" "$ROOT/build/stage"/
( cd "$ROOT/build/stage" && $JAR --create --file "$ROOT/ForgeCore-1.0.0.jar" . )
echo "built $ROOT/ForgeCore-1.0.0.jar"
