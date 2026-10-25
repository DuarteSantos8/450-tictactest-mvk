# TicTacTest

TicTacToe-Spiel in Java, das im Modul 450 (Applikationen testen) mit Tests abgesichert wird.

## Voraussetzungen

- Java 25 (am einfachsten über den DevContainer, siehe [docs/devcontainer.md](docs/devcontainer.md))
- Gradle muss nicht installiert sein, der `./gradlew`-Wrapper lädt die richtige Version.

## Wichtige Befehle

| Befehl | Was passiert |
|---|---|
| `./gradlew run` | Spiel starten (Mensch gegen GreedyPlayer) |
| `./gradlew test` | Tests ausführen + JaCoCo-Report |
| `./gradlew check` | Tests + Coverage-Verification (min. 90 % Branch-Coverage) |
| `./gradlew pitest` | Mutation Testing mit PITest |

Reports liegen danach unter `build/reports/` (`tests/`, `jacoco/`, `pitest/`).

## Dokumentation

- [docs/TESTING.md](docs/TESTING.md) – Testkonzept
- [docs/coverage-design.md](docs/coverage-design.md) – Coverage Time-Series auf GitHub Pages
- [docs/devcontainer.md](docs/devcontainer.md) – DevContainer und Image-Deployment
