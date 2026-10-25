#!/usr/bin/env bash
# Liest die Branch-Coverage (in Prozent) aus einem JaCoCo-XML-Report.
# Aufruf: scripts/coverage.sh [pfad-zum-report.xml]
set -euo pipefail

xml="${1:-build/reports/jacoco/test/jacocoTestReport.xml}"

# Der letzte <counter type="BRANCH" .../> im Report ist die Gesamt-Summe.
line=$(grep -o '<counter type="BRANCH"[^>]*>' "$xml" | tail -1)
covered=$(echo "$line" | grep -o 'covered="[0-9]*"' | grep -o '[0-9]*')
missed=$(echo "$line" | grep -o 'missed="[0-9]*"' | grep -o '[0-9]*')

# Prozent mit zwei Nachkommastellen (z.B. 97.83), damit auch kleine Rueckgaenge auffallen.
awk "BEGIN { printf \"%.2f\", 100 * $covered / ($covered + $missed) }"
