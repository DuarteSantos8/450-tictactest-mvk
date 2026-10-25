# Changelog

Alle wichtigen Änderungen an TicTacTest. Die Versionsnummern folgen `MAJOR.MINOR.PATCH`
(siehe [RELEASE.md](RELEASE.md)). Der neuste Release steht immer zuoberst.

## 1.0.2 - 2026-10-25

### Fixed
- Ungültige Eingaben (keine Zahl, leer, ausserhalb von 0–8 oder ein besetztes Feld) bringen das
  Spiel nicht mehr zum Absturz. Das Spiel meldet „invalid input“ und fragt nochmals.

## 1.0.1 - 2026-10-25

### Fixed
- Tippfehler in der Frage an den Spieler korrigiert: aus „where to to put the next …“
  wird „where to put the next …“.

## 1.0.0 - 2026-10-25

Erste veröffentlichte Version von TicTacTest.

### Added
- Spielbares TicTacToe in der Konsole: ein Mensch spielt gegen den Computer (`GreedyPlayer`).
- Neuer `PerfectPlayer`, der mit dem Minimax-Algorithmus perfekt spielt und nie verliert.
- Ausführbares JAR: das Spiel startet mit `java -jar tictactest-1.0.0.jar`.
- Automatisierte Tests mit JUnit und AssertJ, inkl. Parameterized Tests, StdIn/StdOut-Tests
  (Pioneer), Property-based Tests (jqwik) und Mutation Testing (PITest).
- Coverage-Messung mit JaCoCo: der Build schlägt fehl, wenn die Branch-Coverage unter 90 % fällt.
- CI-Pipeline mit GitHub Actions, Coverage-Verlauf auf GitHub Pages und Coverage-Gate für Pull Requests.
- DevContainer mit Java 25, damit alle in der gleichen Umgebung entwickeln.
- Release-Prozess: ein Git-Tag `vX.Y.Z` erstellt automatisch einen GitHub Release mit dem JAR.

### Changed
- `./gradlew run` leitet die Tastatureingabe an das Spiel weiter, so kann man auch über Gradle spielen.

### Fixed
- Der Mensch-Spieler hat Eingaben verloren, wenn mehrere Züge auf einmal eingegeben wurden
  (z.B. per Pipe). Jetzt wird für alle Züge derselbe Scanner verwendet.
- Leerzeichen vor oder nach der eingegebenen Zahl führen nicht mehr zu einem Fehler.
