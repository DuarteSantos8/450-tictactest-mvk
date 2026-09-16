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

Die bestehenden CI-Workflows laufen in einem eigenen Container-Image aus der GitHub
Container Registry (siehe `.github/workflows/build.yml`, `container.image`).

## Auftrag 3 – Continuous Deployment des DevContainers

Ablauf und Versionierungs-/Freigabe-Konzept:

1. **Versionierung mit Git-Tags** (`v1.0.0`, `v1.0.1`, …). Ein Tag = eine Freigabe.
2. **Build & Push** übernimmt `.github/workflows/devcontainer-release.yml`:
   - **Pull Request** auf `.devcontainer/**`: Image wird nur **testweise gebaut**
     (kein Push) → der Dockerfile wird geprüft.
   - **Tag `v*`**: Image wird gebaut und als
     `…/tictactest-devcontainer:vX.Y.Z` **und** `:latest` in die GHCR **gepusht**.
3. **Freigabe (nur freigegebene Container verwenden):**
   Nur ein Tag löst einen Push aus. Ein normaler Push auf `main` erzeugt **kein**
   neues Image. So kann keine ungetaggte Version in die Pipeline gelangen.
4. **Auto-PR:** Nach einem Release erstellt der Workflow automatisch einen Pull
   Request, der `.devcontainer/devcontainer.json` auf die neue Version `vX.Y.Z`
   pinnt. Erst der **Merge** (= Freigabe durch Review) übernimmt die neue Version.
   (Workflow-Dateien darf das automatische `GITHUB_TOKEN` nicht ändern, deshalb
   pinnt der Auto-PR nur die `devcontainer.json`.)
5. **Neueste Version in der CI:** `.github/workflows/devcontainer-ci.yml` läuft im
   Image `…/tictactest-devcontainer:latest`, nutzt also automatisch die neueste
   freigegebene Version.

### Neues Release erstellen

```
git tag v1.0.1
git push origin v1.0.1
```

Danach: Auto-PR abwarten und mergen.
