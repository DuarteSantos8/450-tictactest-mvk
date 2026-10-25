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

## Release herunterladen und spielen

Fertige Versionen gibt es unter [Releases](https://github.com/DuarteSantos8/450-tictactest-mvk/releases).
JAR herunterladen und mit Java 25 starten:

```bash
java -jar tictactest-1.0.0.jar
```

Selbst bauen: `./gradlew jar` → `build/libs/tictactest-<version>.jar`.

## Dokumentation

- [docs/TESTING.md](docs/TESTING.md) – Testkonzept
- [RELEASE.md](RELEASE.md) – Release-Prozess, [CHANGELOG.md](CHANGELOG.md) – Änderungen pro Version
- [docs/coverage-design.md](docs/coverage-design.md) – Coverage Time-Series auf GitHub Pages
- [docs/devcontainer.md](docs/devcontainer.md) – DevContainer und Image-Deployment
