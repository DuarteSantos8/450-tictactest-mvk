# Testkonzept – TicTacTest (Modul 450)

**Projekt:** TicTacTest (TicTacToe-Spiel-Engine in Java)
**Dokument-Version:** 0.7 (IST)
**Autor / Rolle:** Duarte Santos – Entwickler = Tester (bewertete Einzelarbeit),
Kontakt: duarte.lavouras@lernende.bbw.ch
**Auftraggeber / Projektleitung:** Dominik Berner (BBW, Modul 450)
**Repository:** `DuarteSantos8/450-tictactest-mvk`

Dieses Dokument beschreibt den **aktuellen IST-Zustand** der Test-Suite: was getestet wird,
mit welchen Werkzeugen, nach welcher Strategie und mit welchen Erfolgskriterien. Es
dokumentiert nur die bereits umgesetzten Tests, keine geplanten Erweiterungen.

## 1. Einleitung

Getestet wird das ganze Spiel: die Spiel-Engine `TicTacToeMain` und alle Player.

- **`isWin(Stone[] board, Stone color)`** – Sieg-Erkennung: hat eine Farbe drei in einer
  Linie? Eine reine Funktion über ein beliebiges Brett und Herzstück der Test-Suite.
- **`play(TicTacToePlayer xPlayer, TicTacToePlayer oPlayer)`** – der Spielablauf: eine
  komplette Partie, Sieg, Unentschieden, ungültige Züge und die Vorbedingung, dass beide
  Spieler verschieden sein müssen.
- **`toString(...)`** und **`main(...)`** – Bildschirmausgabe und Programmstart.
- **`HumanPlayer`** (liest von der Tastatur), **`GreedyPlayer`** und der neue, perfekt
  spielende **`PerfectPlayer`** (Minimax-Algorithmus).

## 2. Testziele

Die Ziele sind nummeriert, damit die Testfälle (Abschnitt 7) darauf verweisen können.

- **TZ-01:** `isWin` erkennt alle acht möglichen Siegeslinien für die richtige Farbe.
- **TZ-02:** `isWin` meldet für Bretter ohne Linie (oder für die falsche Farbe) korrekt „kein Sieg".
- **TZ-03:** `play` spielt eine vollständige Partie zweier `GreedyPlayer`; der Startspieler X gewinnt.
- **TZ-04:** `play` weist zwei identische Spieler-Instanzen mit einer `IllegalArgumentException` ab.
- **TZ-05:** `play` erkennt ein Unentschieden und bricht bei ungültigen Zügen mit einer
  `IllegalStateException` ab.
- **TZ-06:** Ein-/Ausgabe funktioniert: `HumanPlayer` liest die Eingabe, `toString` und `main`
  geben das Brett richtig aus (StdIn/StdOut-Tests mit Pioneer).
- **TZ-07:** Der `PerfectPlayer` verliert nie (als X und als O), blockt, wenn der Gegner gewinnen
  könnte, und gewinnt, wenn er kann.
- **TZ-08:** Mindestens 90 % Branch-Coverage (wird im Build erzwungen).
- **TZ-09:** Ein ganzes Spiel über `main()` funktioniert von der ersten Eingabe bis zum Resultat;
  ungültige oder böswillige Eingaben bringen das Spiel nicht zum Absturz oder Hängen (E2E).

## 3. Teststrategie und Teststufen

**Teststufen (Testpyramide):**

| Stufe | Was | Wo | Anzahl |
|---|---|---|---|
| Unit-Tests (Basis) | einzelne Methoden und Player, schnell | `./gradlew test` | 43 |
| End-to-End-Tests (Spitze) | ganzes Spiel über `main()` mit Tastatureingabe und Ausgabe | `./gradlew e2eTest` | 8 |
| Release-Smoke-Test | das fertige JAR wird gestartet und ein Spiel gespielt | `release.yml` | 1 |

Eine eigene Integrationsstufe gibt es nicht: das Zusammenspiel von `play(...)` mit echten Playern
wird schon in den Unit-Tests geprüft (ganze Spiele in `TicTacToeMainTest`). Dazu kommen:

- **StdIn/StdOut-Tests mit Pioneer:** `@StdIo` ersetzt die Tastatur-Eingabe und fängt die
  Konsolen-Ausgabe ab (`HumanPlayerTest`, `main` und `play` in `TicTacToeMainTest`).
- **Property-based Tests mit jqwik** (zusätzliches Testframework): jqwik erzeugt viele
  zufällige Eingaben und prüft, dass eine Regel immer stimmt (`PerfectPlayerPropertiesTest`).
- **Mutation Testing mit PITest:** PITest baut absichtlich Fehler in den Code ein (z.B. `<`
  statt `<=`) und schaut, ob ein Test rot wird. Wird kein Test rot, ist der Test zu schwach.
  Der Build schlägt fehl, wenn weniger als 90 % der Mutanten erkannt werden (`mutationThreshold`).
- **Coverage-Verification mit JaCoCo:** der Build schlägt fehl, wenn die Branch-Coverage
  unter 90 % fällt.

Angewendete Prinzipien:

- **Sprechende Testnamen in camelCase**, die das erwartete Verhalten beschreiben, z.B.
  `playRejectsTheSamePlayerTwice` (Java-Konvention, keine Sonar-Warnung wegen `_`). Wo es
  geht, sind Tests Einzeiler; sonst sind Vorbereitung, Aktion und Prüfung durch Leerzeilen getrennt.
- **AssertJ fluent-Assertions** statt roher `assertTrue(...)`: die Testklasse implementiert
  `WithAssertions`, damit `assertThat(...)` und `assertThatThrownBy(...)` direkt verfügbar sind.
- **Fixtures für Isolation:** benannte Board-Konstanten statt magischer Strings und vor jeder
  Testmethode frische Spieler-Instanzen (`@BeforeEach`).
- **Parameterized Test gegen Coderepetition:** gleiche Testlogik mit anderen Daten läuft immer
  über *eine* Methode: Siegeslinien (`@MethodSource`), ganze Spiele (`@MethodSource`), ungültige
  Züge und Muster (`@ValueSource`), `opponent()` und Spielzüge (`@CsvSource`) und der Test
  „volles Brett“ für alle Computer-Player (`@MethodSource`).

### Fixtures und Helper

**Board-Fixtures** – benannte Konstanten. Ein Brett wird als Muster geschrieben, `X` = Kreuz,
`O` = Kreis, `.` = leeres Feld; die Leerzeichen trennen nur die drei Zeilen:

```java
private static final String DIAGONAL_X_WINS = "XOO OX. XOX";
private static final String DRAW_BOARD = "XOX XXO OXO";
```

**Spieler-Fixture** – `@BeforeEach setUp()` legt in `TicTacToeMainTest` vor *jeder*
Testmethode zwei frische `GreedyPlayer` an, damit kein Test von einer Partie eines anderen Tests beeinflusst wird.

**Helper** – `Boards.toBoard(...)` übersetzt so ein Muster in das `Stone[]` mit neun Feldern,
das das Spiel erwartet, `Boards.emptyBoard()` liefert ein leeres Brett. Er liegt in einer eigenen Klasse, damit alle Testklassen ihn benutzen
können (keine Coderepetition). Bei falscher Länge oder unbekanntem Zeichen wirft er eine
`IllegalArgumentException`.

**Test-Player als Lambda** – `TicTacToePlayer` hat nur eine Methode, darum kann man einen
Test-Player einfach als Lambda schreiben, z.B. einen „schummelnden“ Player, der immer auf
Feld `-1` spielt. So braucht es kein Mocking-Framework.

### Parameterized Test

Die Sieg-Erkennung (TZ-01 / TZ-02) wird mit *einer* Methode über viele Konstellationen geprüft:

```java
@ParameterizedTest(name = "{1} on \"{0}\" -> {2}")
@MethodSource("boardConstellations")
void isWinDetectsALineOnlyForItsOwnColor(String pattern, Stone color, boolean expectedToWin) {
    assertThat(TicTacToeMain.isWin(toBoard(pattern), color)).isEqualTo(expectedToWin);
}
```

## 4. Testobjekte und Testabdeckung

| Testobjekt | Abgedeckt? | Testklasse | Bemerkung |
|---|---|---|---|
| `TicTacToeMain.isWin(...)` | ja | `TicTacToeMainTest` | alle acht Siegeslinien + Nicht-Sieg-Fälle |
| `TicTacToeMain.play(...)` | ja | `TicTacToeMainTest` | 4 ganze Spiele (Sieg X, Sieg O, Unentschieden), ungültige Züge, gleiche Spieler |
| `TicTacToeMain.toString(...)` | ja | `TicTacToeMainTest` | Ausgabe ohne Farbcodes verglichen |
| `TicTacToeMain.main(...)` | ja | `TicTacToeMainTest` | mit Pioneer: Eingabe `3, 4, 5` → Mensch gewinnt |
| `HumanPlayer` | ja | `HumanPlayerTest` | mit Pioneer: Eingabe lesen, ungültige Eingabe / besetztes Feld → nochmals fragen |
| ganzes Spiel über `main()` | ja | `GameEndToEndTest` | E2E: Sieg X, Sieg O, Unentschieden, ungültige/böswillige Eingaben, DoS (Abschnitt 7.1) |
| `GreedyPlayer` | ja | `ComputerPlayersTest` | nimmt das erste freie Feld; volles Brett → Exception |
| `PerfectPlayer` | ja | `PerfectPlayerTest`, `PerfectPlayerPropertiesTest`, `ComputerPlayersTest`, `TicTacToeMainTest` | blockt, gewinnt wenn möglich, gewinnt gegen Greedy, verliert nie als X und als O (jqwik), volles Brett → Exception |
| `Stone.opponent()` | ja | `TicTacToeMainTest` | beide Farben |

**Gemessene Abdeckung (JaCoCo):**

| Metrik | Wert | Ziel |
|---|---|---|
| Branch-Coverage | **100 %** (98 von 98) | min. 90 % (im Build erzwungen) |
| Line-Coverage | **98.6 %** (71 von 72) | – |
| Mutation Score (PITest) | **100 %** (76 von 76 Mutanten gekillt) | min. 90 % (im Build erzwungen) |

Die eine nicht abgedeckte Zeile ist der unsichtbare Standard-Konstruktor von `TicTacToeMain`
(die Klasse hat nur statische Methoden, darum wird nie ein Objekt davon erstellt).

Früher überlebte ein Mutant: `score > bestScore` im `PerfectPlayer` wurde zu `score >= bestScore`.
Dann nimmt der Player bei gleich guten Zügen den letzten statt den ersten Zug. Der Test
`takesTheFirstFieldWhenAllMovesAreEquallyGood` (leeres Brett → Feld 0) erkennt das jetzt.

## 5. Testrahmen und Erfolgskriterien

- **Wer testet:** der Entwickler selbst (Einzelarbeit).
- **Wann wird getestet:** lokal bei jeder Änderung und automatisch bei jedem Push und Pull-Request
  über GitHub Actions.
- **Pass-Kriterium:** alle Tests laufen grün durch (`0 failures, 0 errors`) und die
  Branch-Coverage ist mindestens 90 %.
- **Fail-Kriterium / Abbruchbedingung:** ein einziger roter Test oder zu wenig Coverage lässt
  die CI-Pipeline fehlschlagen. Auf `main` ist Branch Protection aktiv: Änderungen nur per
  Pull Request, und die Checks `build`, `test` und `gate` (Coverage-Gate) müssen grün sein.

## 6. Testumgebung und Testinfrastruktur

- **Sprache / Runtime:** Java 25 (Azul Zulu, bereitgestellt über den DevContainer).
- **Build-Tool:** Gradle 9.8 (über den `./gradlew`-Wrapper).
- **Testframeworks (aktuellste Versionen):** JUnit 6.1.3 (`junit-bom`), AssertJ 3.27.7,
  JUnit Pioneer 2.3.0, jqwik 1.10.2, JaCoCo 0.8.15, PITest 1.30.1.
- **Lokale Ausführung:**
  - `./gradlew test` → Test-Report unter `build/reports/tests/test/index.html`,
    Coverage-Report unter `build/reports/jacoco/test/html/index.html`
  - `./gradlew e2eTest` → nur die E2E-Tests (`@Tag("e2e")`), Report unter `build/reports/tests/e2eTest/`
  - `./gradlew check` → Unit-Tests, E2E-Tests und Coverage-Verification (min. 90 % Branch)
  - `./gradlew pitest` → Mutation-Report unter `build/reports/pitest/index.html`
- **CI:** `.github/workflows/build.yml` (Schritte `build` und `test`, im DevContainer-Image).
  Der Test-Job führt `./gradlew check pitest` aus (also auch die E2E-Tests) und speichert
  Test- (Unit + E2E), Coverage- und PITest-Report bei jedem Commit als Artifacts.
- **Release:** `.github/workflows/release.yml` läuft nur bei einem Tag `vX.Y.Z`. Er führt
  `./gradlew check` aus und startet danach das gebaute JAR als Smoke-Test (Eingabe `3, 4, 5`
  → X gewinnt). Schlägt etwas fehl, wird kein Release erstellt (siehe `RELEASE.md`).
- Der Parameterized Test braucht keine Zusatz-Abhängigkeit: `org.junit.jupiter:junit-jupiter`
  ist ein Sammel-Artefakt und enthält `junit-jupiter-params` bereits.

## 7. Testfallbeschreibungen

| ID | Zielbezug | Voraussetzung | Schritt | Erwartetes Ergebnis |
|---|---|---|---|---|
| TC-01 | TZ-01 | Brett `XXX ... ...` | `isWin(board, CROSS)` | `true` (oberste Reihe) |
| TC-02 | TZ-01 | Brett `..O ..O ..O` | `isWin(board, CIRCLE)` | `true` (rechte Spalte) |
| TC-03 | TZ-01 | Brett `XOO OX. XOX` | `isWin(board, CROSS)` | `true` (Diagonale 0-4-8) |
| TC-04 | TZ-02 | leeres Brett `... ... ...` | `isWin(board, CROSS)` | `false` |
| TC-05 | TZ-02 | Brett `OOO XX. .X.` | `isWin(board, CROSS)` | `false` (Linie gehört O, nicht X) |
| TC-06 | TZ-03 | zwei `GreedyPlayer` | `play(greedy, greedy)` | Rückgabe `CROSS`, Ausgabe „...and the winner is: CROSS“ |
| TC-07 | TZ-04 | dieselbe Spieler-Instanz als X und O | `play(xPlayer, xPlayer)` | `IllegalArgumentException` |
| TC-08 | TZ-05 | zwei `PerfectPlayer` | `play(...)` | Rückgabe `null`, Ausgabe „it's a draw!“ |
| TC-09 | TZ-05 | O spielt auf `-1`, `9` oder das besetzte Feld `0` | `play(greedy, cheater)` | `IllegalStateException`, vorher wird das Brett ausgegeben |
| TC-10 | TZ-06 | Eingabe `" 4 "` | `HumanPlayer.play(...)` | Rückgabe `4`, Frage „where to put…“ wird ausgegeben |
| TC-11 | TZ-06 | Eingaben `abc`, leer, `9`, `-1`, dann `4` | `HumanPlayer.play(...)` | 4 × „invalid input“, dann Rückgabe `4` |
| TC-12 | TZ-06 | Brett `X.. .O. ...` | `toString(board)` | `X  1  2` / `3  O  5` / `6  7  8` |
| TC-13 | TZ-06 | Eingabe `3, 4, 5` | `main(...)` | Ausgabe „...and the winner is: CROSS“ |
| TC-14 | TZ-07 | `GreedyPlayer` gegen `PerfectPlayer` (beide Reihenfolgen) | `play(...)` | `PerfectPlayer` gewinnt |
| TC-15 | TZ-07 | Brett `XX. .O. ...`, O ist dran | `PerfectPlayer.play(...)` | `2` (blockt) |
| TC-16 | TZ-07 | Gegner spielt die Felder in zufälliger Reihenfolge, PerfectPlayer als X oder O (100 Spiele, jqwik) | `play(...)` | der Gegner gewinnt nie |
| TC-17 | TZ-07 | Brett `X.. OO. XX.`, O ist dran | `PerfectPlayer.play(...)` | `5` (gewinnt sofort statt nur zu blocken) |
| TC-18 | TZ-08 | – | `./gradlew check` | Build grün, Branch-Coverage ≥ 90 % |

TC-01 bis TC-05 sind Beispiele aus den 12 Konstellationen des Parameterized Tests
(`boardConstellations`); die vollständige Liste steht in `TicTacToeMainTest.java`.

### 7.1 End-to-End-Testfälle

Klasse `e2e/GameEndToEndTest` (Tag `e2e`, eigener Task `e2eTest`). Das Spiel wird über `main()`
gestartet (Mensch = X, GreedyPlayer = O), Pioneer `@StdIo` ersetzt Tastatur und Konsole. Jeder
Test hat ein Timeout von 5 s in einem eigenen Thread, damit ein hängendes Spiel abgebrochen wird.

| ID | Zielbezug | Eingabe | Erwartetes Verhalten | Tatsächliches Verhalten |
|---|---|---|---|---|
| E2E-01 | TZ-09 | `3, 4, 5` | „...and the winner is: CROSS“ | wie erwartet |
| E2E-02 | TZ-09 | `8, 7, 3` | O füllt 0, 1, 2: „...and the winner is: CIRCLE“ | wie erwartet |
| E2E-03 | TZ-09 | `1, 3, 4, 6, 8` | Brett voll: „it's a draw!“ | wie erwartet |
| E2E-04 | TZ-09 | `abc`, `3`, leer, `4`, `-1`, `99999999999999999999`, `5` | 4 × „invalid input“, Spiel läuft weiter, X gewinnt | vor dem Fix: Absturz mit `NumberFormatException` (F-01); jetzt wie erwartet |
| E2E-05 | TZ-09 | `3`, `0` (von O besetzt), `3` (eigenes), `4`, `5` | 2 × „invalid input“, X gewinnt | vor dem Fix: Absturz mit `IllegalStateException` (F-02); jetzt wie erwartet |
| E2E-06 | TZ-09 | Steuerzeichen `ESC[2J`, `4; rm -rf /`, `%s%n`, dann `3, 4, 5` | abgelehnt und **nicht** wieder ausgegeben, X gewinnt | wie erwartet |
| E2E-07 | TZ-09 | `abc`, dann Ende der Eingabe (Ctrl+D) | Spiel endet sofort, wartet nicht endlos (DoS) | endet mit `NoSuchElementException` (F-03, offen) |
| E2E-08 | TZ-09 | – (PerfectPlayer gegen PerfectPlayer) | ganzes Spiel in < 1 s (DoS durch langsamen Minimax) | wie erwartet (ca. 0.04 s) |

### 7.2 Gefundene Fehler

| ID | Fehler | Gefunden mit | Status |
|---|---|---|---|
| F-01 | Eingabe ohne Zahl (`abc`, leer, zu grosse Zahl) bringt das Spiel mit `NumberFormatException` zum Absturz | E2E-04 | **behoben test-first**: zuerst `HumanPlayerTest.asksAgainUntilTheInputIsAFieldOnTheBoard` (rot), dann Fix im `HumanPlayer` (grün) |
| F-02 | Zahl ausserhalb 0–8 oder besetztes Feld beendet das Spiel mit `IllegalStateException` | E2E-05 | **behoben** mit dem gleichen Fix: der `HumanPlayer` fragt nochmals |
| F-03 | Endet die Eingabe mitten im Spiel, stürzt das Programm mit `NoSuchElementException` ab, statt sauber zu beenden | E2E-07 | offen (kein Hängen, darum kein DoS; nur unschöne Fehlermeldung) |

## 8. Testplan und Zuständigkeiten

- **Zuständig:** Duarte Santos (Erstellung, Ausführung und Wartung der Tests).
- **Ausführung:** automatisiert bei jedem Push / Pull-Request über GitHub Actions; zusätzlich
  lokal vor jedem Commit.
- **Ergebnisdokumentation:** Test-Report und JaCoCo-Coverage werden in der CI als Artifact
  gespeichert; die Coverage-Entwicklung ist als Time-Series auf GitHub Pages sichtbar.

## 9. Testumfang

**Gesamt: 51 Tests** (43 Unit + 8 E2E), zuletzt lokal mit `./gradlew clean check pitest` ausgeführt: **0 Fehler**.

| Testklasse | Anzahl | Inhalt |
|---|---|---|
| `TicTacToeMainTest` | 26 | 12 × `isWin`, 4 ganze Spiele, 3 × ungültiger Zug, 2 × falsches Muster, 2 × `opponent()`, 3 Einzeltests |
| `ComputerPlayersTest` | 5 | 2 × volles Brett (Greedy + Perfect), 3 × Zug des GreedyPlayer |
| `PerfectPlayerTest` | 6 | 3 × blocken, 2 × gewinnen, 1 × erstes von gleich guten Feldern |
| `HumanPlayerTest` | 4 | StdIn/StdOut mit Pioneer, ungültige Eingabe, besetztes Feld |
| `PerfectPlayerPropertiesTest` | 2 | jqwik-Properties (100 bzw. 50 Durchläufe) |
| `GameEndToEndTest` | 8 | E2E-01 bis E2E-08 (eigener Task `e2eTest`) |

Parameterized Tests zählen pro Datensatz als eigener Test. Der frühere `DummyTest` (nur Nachweis,
dass JUnit und AssertJ eingebunden sind) wurde gelöscht, weil das die echten Tests jetzt zeigen.

Bei jedem Push/Pull-Request läuft zusätzlich die GitHub-Actions-Pipeline; der Build ist grün.

## 10. Screenshots

Alle Unit-Tests erfolgreich (Stand 42 Tests, vor den E2E-Tests):

![Alle Tests erfolgreich (42/42, 100%)](screenshots/all-tests-passing.png)

Ein bewusst fehlgeschlagener Test (zum Nachweis, damals mit 36 Tests; danach wieder zurückgesetzt) – Übersicht,
Klassendetail und Stacktrace:

![Ein Test schlägt fehl (Übersicht)](screenshots/one-test-failing-overview.png)

![Ein Test schlägt fehl (Klassendetail)](screenshots/one-test-failing-detail.png)

![Stacktrace des fehlschlagenden Tests](screenshots/one-test-failing-stacktrace.png)
