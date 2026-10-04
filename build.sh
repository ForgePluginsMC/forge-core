#!/usr/bin/env bash
# ForgeCore direct-javac build (Gradle daemon cannot run in this sandbox).
set -euo pipefail
ROOT="$HOME/workspace/forge-core"
DEPS="$HOME/workspace/.toolchains/paper-deps"
VAULT="$HOME/workspace/.toolchains/vault"
JAVAC="$HOME/workspace/.toolchains/jdk-25.0.4.1+1/bin/javac"
JAR="$HOME/workspace/.toolchains/jdk-25.0.4.1+1/bin/jar"
CP=$(ls "$DEPS"/*.jar "$VAULT"/*.jar 2>/dev/null | tr '\n' ':')

rm -rf "$ROOT/build" && mkdir -p "$ROOT/build/classes" "$ROOT/build/stage"
find "$ROOT/src/main/java" -name '*.java' > "$ROOT/build/sources.txt"
$JAVAC -Werror -Xlint:deprecation -parameters -d "$ROOT/build/classes" -cp "$CP" @"$ROOT/build/sources.txt"
cp -r "$ROOT/build/classes"/. "$ROOT/build/stage"/
# Bundle Vault economy API so ForgeCore IS the Vault provider (no separate Vault needed)
cp -r "$ROOT/vault-api"/. "$ROOT/build/stage"/
cp -r "$ROOT/src/main/resources/." "$ROOT/build/stage"/
( cd "$ROOT/build/stage" && $JAR --create --file "$ROOT/ForgeCore-1.0.0.jar" . )
echo "built $ROOT/ForgeCore-1.0.0.jar"
