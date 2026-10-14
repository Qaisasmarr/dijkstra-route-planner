#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p build/classes build/test-classes
find src/main/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -d build/classes
find src/test/java -name '*.java' -print0 | xargs -0 javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes
java -cp build/classes:build/test-classes com.qais.routeplanner.RouteEngineTest
