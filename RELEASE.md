# Release-Prozess

So wird aus einem Stand im Git-Repository eine veröffentlichte Version von TicTacTest
(ein GitHub Release mit ausführbarem JAR). Die Anleitung ist so geschrieben, dass sie jemand
anderes ohne Rückfrage durchführen kann.

## Ablauf auf einen Blick

```
Entwicklung (Feature-Branch + Pull Request)
    ↓
Änderungen nach main (PR gemergt, CI grün)
    ↓
Release-PR: CHANGELOG.md + Version in build.gradle anpassen → mergen
    ↓
Git-Tag vX.Y.Z auf den main-Commit setzen und pushen
    ↓
GitHub Actions (release.yml) startet
    ↓
Bedingungen prüfen → Tests → Build → JAR erstellen → JAR testen
    ↓
GitHub Release erstellen + JAR anhängen
    ↓
JAR herunterladen und testen
```

## Versionsnummer (MAJOR.MINOR.PATCH)

Die Version steht an **einer** Stelle: `version = '1.0.0'` in `build.gradle`.
Daraus entsteht auch der Name vom JAR (`tictactest-1.0.0.jar`). Der Git-Tag ist die gleiche
Nummer mit einem `v` davor (`v1.0.0`). Der Release-Workflow bricht ab, wenn Tag und Version
nicht zusammenpassen.

Wann wird welche Zahl erhöht?

| Teil | Wann? | Beispiel bei TicTacTest |
|---|---|---|
| **MAJOR** (`2.0.0`) | Änderung, die nicht mehr kompatibel ist | Brett wird 4×4, das `TicTacToePlayer`-Interface ändert sich |
| **MINOR** (`1.1.0`) | neue Funktion, alles Bisherige geht weiter | neuer Player, Spielstand zählen, Menü zum Gegner auswählen |
| **PATCH** (`1.0.1`) | nur Fehler behoben oder kleine Korrektur | Tippfehler in der Ausgabe, falsche Eingabe besser abfangen |

Wird eine Zahl erhöht, werden die Zahlen rechts davon wieder `0` (z.B. `1.2.3` → `1.3.0`).
Änderungen nur an Tests, CI oder Doku brauchen keinen eigenen Release, sie kommen mit dem
nächsten Release mit.

## Bedingungen vor einem Release

| Bedingung | Wie wird sie sichergestellt? |
|---|---|
| Alle geplanten Änderungen sind auf `main` | Branch Protection: auf `main` kommt nur, was per Pull Request mit grünen Checks (`build`, `test`, `gate`) gemergt wurde; der Workflow prüft, dass der Tag-Commit auf `main` liegt |
| Das Projekt lässt sich bauen | CI (`build.yml`) ist auf `main` grün; der Workflow baut nochmals |
| Alle Tests sind erfolgreich | CI ist grün; der Workflow führt `./gradlew check` aus (Tests + min. 90 % Branch-Coverage) |
| Das Changelog ist aktualisiert | Der Workflow bricht ab, wenn `CHANGELOG.md` keinen Abschnitt `## X.Y.Z - Datum` hat |
| Die Versionsnummer ist festgelegt | `version` in `build.gradle` muss genau zum Tag passen, sonst Abbruch |

## Schritt für Schritt

1. **Version festlegen** nach der Tabelle oben, z.B. `1.0.1`.
2. **Release-PR erstellen:** auf einem Branch
   - `version = '1.0.1'` in `build.gradle` setzen,
   - in `CHANGELOG.md` zuoberst einen neuen Abschnitt `## 1.0.1 - JJJJ-MM-TT` mit
     `### Added` / `### Changed` / `### Fixed` schreiben,
   - Pull Request erstellen, warten bis die CI grün ist, mergen.
3. **Lokal auf den neusten `main` wechseln:**
   ```bash
   git checkout main
   git pull
   ```
4. **Tag erstellen und pushen:**
   ```bash
   git tag -a v1.0.1 -m "Release v1.0.1"
   git push origin v1.0.1
   ```
5. **Workflow beobachten:** GitHub → Actions → `release`. Danach steht der Release unter
   GitHub → Releases.
6. **Release testen** (siehe unten).

## Fragen und Antworten

**Wann wird ein Release erstellt?**
Wenn bewusst entschieden wird, dass der aktuelle Stand von `main` veröffentlicht werden soll,
z.B. nach einer neuen Funktion oder einem behobenen Fehler. Nicht nach jeder Änderung.

**Wer bzw. was startet einen Release?**
Eine Person (hier: der Entwickler) startet ihn, indem sie einen Tag `vX.Y.Z` pusht. Der Push vom
Tag startet automatisch den Workflow `.github/workflows/release.yml`.

**Warum löst nicht jeder Push auf `main` einen Release aus?**
Auf `main` kommen auch Änderungen an Tests, CI oder Doku, die für Benutzer nichts ändern. Jeder
Push würde sonst eine neue Version erzeugen, ohne dass Changelog und Versionsnummer geplant sind.
Darum reagiert der Workflow nur auf `on: push: tags: 'v*'`.

**Wie wird die Versionsnummer festgelegt?**
Nach `MAJOR.MINOR.PATCH` (Tabelle oben). Sie wird in `build.gradle` eingetragen und muss zum Tag passen.

**Wie wird ein Release-Tag erstellt?**
Mit `git tag -a vX.Y.Z -m "Release vX.Y.Z"` auf dem aktuellen `main` und `git push origin vX.Y.Z`.
`-a` erstellt einen annotierten Tag (mit Autor, Datum und Nachricht).

**Welcher Commit gehört zu einem Release?**
Genau der Commit, auf den der Tag zeigt. Er steht auch im Text vom GitHub Release (`Commit: …`).
Nachschauen geht mit `git show v1.0.0` oder auf GitHub unter Tags.

**Wie wird das JAR gebaut?**
Mit Gradle: `./gradlew jar` erzeugt `build/libs/tictactest-X.Y.Z.jar`. Im Manifest steht die
`Main-Class`, darum startet es mit `java -jar`. Im Workflow läuft der Build im DevContainer-Image
(Java 25), also in der gleichen Umgebung wie lokal und in der CI.

**Wo kann das JAR heruntergeladen werden?**
Auf GitHub unter **Releases** (`https://github.com/DuarteSantos8/450-tictactest-mvk/releases`),
bei jedem Release unter „Assets“. Dazu gibt es eine `.sha256`-Datei mit der Prüfsumme.

**Was enthält das Changelog?**
Pro Version: Versionsnummer, Datum und was sich für Benutzer oder Entwickler geändert hat,
eingeteilt in `Added` (neu), `Changed` (geändert) und `Fixed` (behoben). Keine Liste von Commits.
Der Abschnitt der Version wird automatisch in den Text vom GitHub Release übernommen.

**Was passiert, wenn beim Release ein Test fehlschlägt?**
Der Workflow stoppt beim Schritt „Tests ausführen“. Es wird **kein** Release erstellt und kein
JAR veröffentlicht. Dann:
1. Fehler auf einem Branch beheben und per Pull Request nach `main` bringen.
2. Den fehlgeschlagenen Tag löschen: `git tag -d vX.Y.Z` und `git push origin :refs/tags/vX.Y.Z`.
3. Tag auf dem neuen `main`-Commit nochmals erstellen und pushen.

Da die Version noch nie veröffentlicht wurde, darf die gleiche Nummer wiederverwendet werden.
Ein bereits veröffentlichter Release wird nie überschrieben, sondern mit einer neuen Version korrigiert.

## Release testen

Ein Release gilt erst als erfolgreich, wenn das veröffentlichte JAR getestet wurde:

1. Auf GitHub den Release öffnen und `tictactest-X.Y.Z.jar` herunterladen
   (oder `gh release download vX.Y.Z`).
2. Optional die Prüfsumme kontrollieren: `shasum -a 256 -c tictactest-X.Y.Z.jar.sha256`.
3. Starten (Java 25 nötig): `java -jar tictactest-X.Y.Z.jar` und eine Runde spielen.

Der Workflow macht zusätzlich einen automatischen Smoke-Test: er startet das JAR und spielt
ein Spiel (Eingabe `3`, `4`, `5`), das X gewinnen muss.

**Protokoll (manueller Test nach dem Release):**

| Release | Download | Prüfsumme | `java -jar` mit Eingabe `3, 4, 5` (Java 25) | Resultat |
|---|---|---|---|---|
| v1.0.0 (25.10.2026) | `gh release download v1.0.0` | `tictactest-1.0.0.jar: OK` | „...and the winner is: CROSS“ | erfolgreich |
| v1.0.1 (25.10.2026) | `gh release download v1.0.1` | `tictactest-1.0.1.jar: OK` | Frage „where to put the next CROSS?“, „...and the winner is: CROSS“ | erfolgreich |

## Hinweis zu älteren Tags

Früher wurden die Tags `v1.0.0`–`v1.0.2` für die Versionen vom DevContainer-Image benutzt. Diese
Tags wurden gelöscht, damit `vX.Y.Z` eindeutig nur noch App-Releases bezeichnet. Das
DevContainer-Image hat eigene Versionen in der GitHub Container Registry
(siehe [docs/devcontainer.md](docs/devcontainer.md)).

Der erste Versuch für `v1.0.0` ist im Workflow bei der Prüfung „Tag-Commit liegt auf `main`“
abgebrochen (Git im Container: „dubious ownership“), **bevor** etwas veröffentlicht wurde. Nach
dem Fix (PR #6) wurde der Tag gemäss Abschnitt „Was passiert, wenn … fehlschlägt?“ gelöscht und
auf dem neuen `main`-Commit nochmals gesetzt.
