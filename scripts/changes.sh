#!/bin/bash
# Findet heraus, ob sich Backend und/oder Frontend seit dem letzten Release-Tag geaendert haben.
# Aufruf: scripts/changes.sh v1.2.0   -> gibt "backend=true|false" und "frontend=true|false" aus
# Verglichen wird mit dem vorherigen Release-Tag, nicht mit dem letzten Commit (ein Release kann viele Commits haben).
set -euo pipefail
TAG="$1"

# vorheriger Release-Tag (nur v*, andere Tags wie backup/* zaehlen nicht)
if ! PREVIOUS=$(git describe --tags --abbrev=0 --match 'v*' "$TAG^" 2>/dev/null); then
  # allererster Release: es gibt nichts zum Vergleichen, also alles deployen
  echo "kein vorheriger Release, alles wird deployt" >&2
  echo "backend=true"
  echo "frontend=true"
  exit 0
fi
echo "vergleiche $PREVIOUS..$TAG" >&2
git diff --name-only "$PREVIOUS" "$TAG" >&2

# Backend = alles, was ins JAR kommt
BACKEND=false
if [ -n "$(git diff --name-only "$PREVIOUS" "$TAG" -- src/main settings.gradle gradle)" ]; then
  BACKEND=true
fi
# build.gradle zaehlt nur, wenn sich mehr als die Versionsnummer geaendert hat (die aendert sich bei jedem Release)
if git diff -U0 "$PREVIOUS" "$TAG" -- build.gradle | grep '^[+-][^+-]' | grep -qv "^[+-]version = "; then
  BACKEND=true
fi

# Frontend = der Ordner frontend/ (config.js kommt nicht ins Archiv, zaehlt aber trotzdem als Frontend-Code)
FRONTEND=false
if [ -n "$(git diff --name-only "$PREVIOUS" "$TAG" -- frontend)" ]; then
  FRONTEND=true
fi

echo "backend=$BACKEND"
echo "frontend=$FRONTEND"
