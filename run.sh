#!/usr/bin/env bash
# ---------------------------------------------------------------------------
# Compiles and runs the clinic appointment demo (Java 8) from Git Bash / macOS
# / Linux. If javac is not on your PATH, set JAVA_HOME first, e.g.
#   export JAVA_HOME="/c/Program Files/Java/jdk1.8.0_271"
# ---------------------------------------------------------------------------
set -e

cd "$(dirname "$0")"

JAVAC="${JAVA_HOME:+$JAVA_HOME/bin/}javac"
JAVA="${JAVA_HOME:+$JAVA_HOME/bin/}java"

mkdir -p out
"$JAVAC" -encoding UTF-8 -d out src/clinic/*.java src/clinic/staff/*.java src/clinic/booking/*.java
"$JAVA" -Dfile.encoding=UTF-8 -cp out clinic.AssignmentOne
