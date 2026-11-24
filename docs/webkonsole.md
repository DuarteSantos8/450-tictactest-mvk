# TicTacTest im Browser (Web-Konsole)

Das Spiel bleibt eine Konsolen-App. Ein Werkzeug startet für jeden Besucher das Release-JAR und zeigt dessen
Ein- und Ausgabe in einem Terminal im Browser an. Dazu kommt ein kleines statisches Frontend (`frontend/`).

```
Browser ── Frontend (index.html, bettet das Terminal per <iframe> ein)
   │
   └── WebSocket ── ttyd ── Pseudo-Terminal ── java -jar tictactest-X.Y.Z.jar   (ein Prozess pro Verbindung)
```

## Werkzeug: ttyd

| Werkzeug | Bewertung |
|---|---|
| **ttyd** ✅ | Ein einzelnes Programm, echtes Terminal (xterm.js im Browser), ANSI-Farben gehen direkt, wird aktiv gepflegt |
| GoTTY | gleiche Idee, aber seit Jahren kaum noch gepflegt |
| websocketd | überträgt nur Textzeilen, kein Terminal. Man müsste eine eigene Seite schreiben und die ANSI-Farbcodes selbst umwandeln |

Gewählt: **ttyd** (getestet mit 1.7.7), weil es ohne eigenen Code funktioniert und die Farben vom Spielfeld richtig anzeigt.

Wie es funktioniert: Öffnet jemand die Seite, baut der Browser eine **WebSocket**-Verbindung zu ttyd auf. ttyd startet dann
einen neuen `java`-Prozess in einem **Pseudo-Terminal** (PTY). Für das Spiel sieht das aus wie ein normales Terminal.
Tastendrücke gehen über den WebSocket zum Prozess, die Ausgabe zurück in den Browser.

## Lokal starten (nur aus Release-Assets)

Voraussetzung: Java 25, `ttyd` (`brew install ttyd`), Python 3.

```bash
mkdir tictactest-lokal && cd tictactest-lokal
gh release download v1.1.0 -R DuarteSantos8/450-tictactest-mvk
shasum -a 256 -c tictactest-1.1.0.jar.sha256

# Backend-Version aus dem Manifest im JAR lesen (kommt aus build.gradle = Release-Tag)
unzip -p tictactest-1.1.0.jar META-INF/MANIFEST.MF | grep Implementation-Version | cut -d' ' -f2 | tr -d '\r' > version.txt

# Frontend entpacken, config.js für lokal dazulegen
mkdir frontend && unzip tictactest-frontend-1.1.0.zip -d frontend
curl -o frontend/config.js https://raw.githubusercontent.com/DuarteSantos8/450-tictactest-mvk/main/frontend/config.js
curl -O https://raw.githubusercontent.com/DuarteSantos8/450-tictactest-mvk/main/scripts/version-server.py

# drei Terminals:
ttyd -W -i 127.0.0.1 -p 7681 java -jar tictactest-1.1.0.jar   # Web-Konsole
python3 version-server.py 7682                                 # Versionsabfrage
cd frontend && python3 -m http.server 8000                     # Frontend
```

Dann <http://localhost:8000> öffnen.

| Option | Wozu |
|---|---|
| `-W` | Eingaben erlauben. Ohne `-W` ist ttyd seit Version 1.7 nur lesend, man könnte nicht tippen. |
| `-p 7681` | Port der Web-Konsole |
| `-i 127.0.0.1` | nur lokal erreichbar (in der Cloud steht später nginx davor) |
| `-m 5` | höchstens 5 gleichzeitige Verbindungen (für den Server, siehe unten) |

## Beobachtungen

| Test | Ergebnis |
|---|---|
| Ungültige Eingabe (`x`, `45`, besetztes Feld) | Das Spiel meldet `invalid input …` und fragt nochmals. Nach 5 Fehlern bricht es ab (Fix aus 1.0.3). |
| Spiel zu Ende | Der Java-Prozess beendet sich (Exit-Code 0), ttyd schliesst die Verbindung. Mit <kbd>Enter</kbd> verbindet sich das Terminal neu, ttyd startet einen neuen Prozess, das heisst ein neues Spiel. |
| Zwei Tabs offen | **Zwei** Java-Prozesse, je ca. 47 MB RAM (gemessen mit `ps`). Jeder Besucher bekommt eine eigene JVM. |

Für einen Server mit 1 GB RAM heisst das: Das Betriebssystem und ttyd brauchen auch Speicher, und eine JVM kann mehr
belegen als die gemessenen 47 MB (der Standard-Heap ist bis 1/4 vom RAM). Realistisch sind etwa 5 bis 10 Spiele
gleichzeitig. Darum begrenzt man die Verbindungen (`-m`) und eventuell den Heap (`java -Xmx64m -jar …`, das JAR bleibt
dabei unverändert).

## Sicherheit

Die Web-Konsole ist für alle erreichbar, die die URL kennen. ttyd führt genau den Befehl aus, den man ihm gibt.

- **Nie** `ttyd bash` (oder `sh`, `login`) starten. Dann hätte jeder Besucher eine Shell auf dem Server mit den Rechten
  des ttyd-Benutzers: Dateien lesen und löschen, Programme installieren (z.B. Krypto-Miner), andere Server angreifen,
  Zugangsdaten auf dem Server stehlen.
- ttyd startet nur `java -jar tictactest-….jar`. Endet das Spiel, endet auch der Prozess, man fällt nicht in eine Shell.
- In der Cloud läuft ttyd als eigener Benutzer **ohne root** (Auftrag 2), und auf der Instanz liegen keine Access Keys.
- `-m` begrenzt die Anzahl Prozesse, damit der Server nicht vollläuft.

## Versionsabfrage

Das JAR selbst hat keine Versionsabfrage, und wir ändern es nicht. Darum liegt neben dem JAR eine Datei `version.txt`,
die per HTTP ausgeliefert wird:

```bash
curl http://localhost:7682/version.txt   # → 1.1.0
```

Die Version wird **nicht von Hand** geschrieben. Sie kommt aus dem Manifest im JAR (`Implementation-Version`). Diesen Wert
setzt Gradle aus `build.gradle`, und der Release-Workflow prüft, dass er zum Tag passt. Beim Deployment wird `version.txt`
also immer aus genau dem JAR erzeugt, das danach läuft.

Lokal liefert `scripts/version-server.py` die Datei aus, in der Cloud macht das nginx.

## Frontend

`frontend/` enthält nur HTML, CSS und JavaScript (kein Framework, kein Build):

| Datei | Inhalt |
|---|---|
| `index.html` | Titel, Spielregeln, Terminal als `<iframe>`, Anzeige der beiden Versionen |
| `style.css` | Aussehen |
| `app.js` | setzt die Terminal-URL, lädt die Backend-Version und die Frontend-Version (`version.txt`) |
| `config.js` | **Konfiguration pro Umgebung**: `terminalUrl` und `versionUrl` |

`config.js` wird vor `app.js` geladen. Im Repo zeigt sie auf `localhost`. In der Cloud schreibt die Infrastruktur
(`infra/setup.sh`) eine eigene `config.js` (siehe [deployment.md](deployment.md)). Darum ist `config.js` **nicht** im Frontend-Archiv vom
Release: Ein Deployment des Frontends kann die Konfiguration der Umgebung so nicht überschreiben.

**CORS:** Frontend (Port 8000) und Versionsabfrage (Port 7682) sind verschiedene *Origins*. Der Browser erlaubt
`fetch()` auf eine andere Origin nur, wenn der Server das mit dem Header `Access-Control-Allow-Origin` erlaubt. Ohne den
Header meldet die Entwickler-Konsole einen CORS-Fehler und die Backend-Version bleibt "nicht erreichbar".
`version-server.py` setzt den Header deshalb. Das `<iframe>` für das Terminal braucht kein CORS.

## Release

Ein Tag `v*` erzeugt jetzt (siehe `.github/workflows/release.yml`):

| Asset | Inhalt |
|---|---|
| `tictactest-X.Y.Z.jar` + `.sha256` | das Spiel, unverändert |
| `tictactest-frontend-X.Y.Z.zip` + `.sha256` | Frontend mit `version.txt`, ohne `config.js` |

Start-Skript, systemd-Unit und nginx-Konfiguration kommen **nicht** in den Release. Sie beschreiben, *wie und wo* die
App läuft, und gehören darum zur Infrastruktur (Auftrag 2). Der Release sagt nur, *welche Version* läuft.

**Warum das Frontend als Release-Asset?** Obwohl es nur ein paar Dateien sind:
- Jede Version ist fest abgelegt. Man kann genau diese Version wieder deployen (Rollback), auch wenn `main` schon
  weiter ist.
- Deployt wird genau das, was im Release steht, und nicht das, was gerade zufällig im Repo liegt.
- Die Frontend-Version (`version.txt`) entsteht automatisch beim Release.
- Mit der Prüfsumme sieht man, ob der Download verändert wurde.
