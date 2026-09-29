# DevContainer & Image-Deployment

Doku zur Aufgabe „DevContainer für TicTacTest" (Auftrag 1–3).

## Auftrag 1 – DevContainer

- Definition liegt in `.devcontainer/Dockerfile` (Alpine-basiert, Java 25).
- Es wird ein Benutzer `dev` mit **UID:GID 1000:1000** angelegt (nicht als root arbeiten).
- `.devcontainer/devcontainer.json` verwendet das fertige Image aus der GHCR und
  installiert die VSCode-Extensions **Java Extension Pack** + **Gradle**.
- Gradle und JUnit kommen über den `./gradlew`-Wrapper bzw. `build.gradle`.

Lokal öffnen: in VSCode „Reopen in Container". Da das Image privat ist, vorher
einmal `docker login ghcr.io` (oder das Package in den Package-Settings auf public
stellen).

## Auftrag 2 – Image aus der GHCR in der CI

Alle CI-Workflows (`build.yml`, `coverage-pages.yml`, `coverage-gate.yml`,
`devcontainer-ci.yml`) laufen im selben eigenen Image aus der GitHub Container Registry:
`ghcr.io/duartesantos8/tictactest-devcontainer:vX.Y.Z` (siehe `container.image`). Da das
Image vom eigenen Account stammt, genügt das automatische `GITHUB_TOKEN` zum Pullen.

## Auftrag 3 – Continuous Deployment des DevContainers

Ablauf (`.github/workflows/devcontainer-release.yml`):

1. **Pull Request**, der `.devcontainer/**` ändert: Image wird nur **testweise gebaut**
   (kein Push) → der Dockerfile wird geprüft.
2. **Push auf `main`**, der `.devcontainer/Dockerfile` ändert: Image wird gebaut und als
   `…/tictactest-devcontainer:v1.0.<Lauf-Nummer>` (und `:latest`) in die GHCR **gepusht**.
   Die Version zählt so automatisch hoch.
3. **Auto-PR:** Nach dem erfolgreichen Push erstellt der Workflow automatisch einen Pull
   Request, der die neue Version in `.devcontainer/devcontainer.json` **und in allen
   CI-Workflows** einträgt.
4. **Freigabe:** Alle (lokal und CI) verwenden immer die Version, die in den Dateien steht –
   nie `:latest`. Ein neues Image wird also erst verwendet, wenn der Auto-PR **gemergt**
   ist (= Freigabe durch Review). Nach dem Merge nutzen CI und „Reopen in Container"
   automatisch die neue Version.

Hinweis: Nur Änderungen am `Dockerfile` lösen ein neues Image aus. Sonst würde der Merge
vom Auto-PR (ändert `devcontainer.json`) wieder ein Image bauen → Endlosschleife.

### Token für den Auto-PR

Workflow-Dateien darf das automatische `GITHUB_TOKEN` nicht ändern. Darum braucht es ein
Personal Access Token (classic) mit den Scopes `repo` und `workflow`, gespeichert als
Repository-Secret **`PAT_TOKEN`** (Settings → Secrets and variables → Actions).
