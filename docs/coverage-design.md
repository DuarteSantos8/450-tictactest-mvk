# Design: Coverage Time-Series auf GitHub Pages

Kurzes Design der Lösung (Auftrag 2). Umgesetzt in `coverage-pages.yml` + `pages/index.html`.

## Design-Fragen

- **Woher kommt der Coverage-Wert?**
  Aus dem JaCoCo-XML-Report (`build/reports/jacoco/test/jacocoTestReport.xml`),
  ausgelesen von `scripts/coverage.sh`.
- **Welche Metrik?**
  Line-Coverage in Prozent (eine Nachkommastelle).
- **Wo/wie werden historische Daten gespeichert?**
  Als CSV (`coverage-history.csv`) auf dem `gh-pages`-Branch.
  Format: `Datum,Commit,Coverage`.
- **Wie kommen neue Werte dazu?**
  Der Workflow liest die bestehende CSV, hängt eine neue Zeile an und
  veröffentlicht sie wieder.
- **Wie entsteht die Time-Series?**
  `pages/index.html` lädt die CSV und zeichnet ein Liniendiagramm (Chart.js).
- **Wie wird veröffentlicht?**
  Der `site/`-Ordner wird auf den `gh-pages`-Branch gepusht, GitHub Pages
  serviert ihn.
- **Wann wird aktualisiert?**
  Bei jedem Push auf `main` (und manuell per `workflow_dispatch`).

## Architektur

```
Push auf main
      │
      ▼
GitHub Actions (coverage-pages.yml)
  1. Tests + JaCoCo   -> jacocoTestReport.xml
  2. coverage.sh      -> Coverage in %
  3. alte CSV holen (gh-pages) + neue Zeile anhängen
  4. index.html + CSV nach site/
  5. push -> gh-pages
      │
      ▼
GitHub Pages  ->  Liniendiagramm der Coverage über die Zeit
```
