#!/usr/bin/env bash
# Liest die Line-Coverage (in Prozent) aus einem JaCoCo-XML-Report.
# Aufruf: scripts/coverage.sh [pfad-zum-report.xml]
set -euo pipefail

xml="${1:-build/reports/jacoco/test/jacocoTestReport.xml}"

# Der letzte <counter type="LINE" .../> im Report ist die Gesamt-Summe.
line=$(grep -o '<counter type="LINE"[^>]*>' "$xml" | tail -1)
covered=$(echo "$line" | grep -o 'covered="[0-9]*"' | grep -o '[0-9]*')
missed=$(echo "$line" | grep -o 'missed="[0-9]*"' | grep -o '[0-9]*')

# Prozent auf eine Nachkommastelle gerundet ausgeben (z.B. 76.4).
awk "BEGIN { printf \"%.1f\", 100 * $covered / ($covered + $missed) }"
