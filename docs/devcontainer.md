# DevContainer & Image-Deployment

Doku zur Aufgabe „DevContainer für TicTacTest" (Auftrag 1–3).

## Auftrag 1 – DevContainer

- Definition liegt in `.devcontainer/Dockerfile` (Alpine-basiert, Java 25).
- Es wird ein Benutzer `dev` mit **UID:GID 1000:1000** angelegt (nicht als root arbeiten).
- `.devcontainer/devcontainer.json` baut den Container aus diesem Dockerfile und
  installiert die VSCode-Extensions **Java Extension Pack** + **Gradle**.
- Gradle und JUnit kommen über den `./gradlew`-Wrapper bzw. `build.gradle`.

Lokal öffnen: in VSCode „Reopen in Container".

## Auftrag 2 – Image aus der GHCR in der CI

Die CI-Workflows laufen in einem eigenen Container-Image aus der GitHub Container
Registry (GHCR). Das Image wird gebaut und mit `:latest` in die GHCR gepusht, die
Pipeline zieht es über `container.image` (siehe `.github/workflows/build.yml`).

## Auftrag 3 – Continuous Deployment des DevContainers

Ablauf und Versionierungs-/Freigabe-Konzept:

1. **Versionierung mit Git-Tags** (`v1.0.0`, `v1.0.1`, …).
   Ein Tag = eine bewusste Freigabe.
2. **Build & Push** übernimmt `.github/workflows/devcontainer-release.yml`:
   - **Pull Request** auf `.devcontainer/**`: Image wird nur **testweise gebaut**
     (kein Push) → der Dockerfile wird geprüft.
   - **Tag `v*`**: Image wird gebaut und als `…/tictactest-devcontainer:vX.Y.Z`
     **und** `:latest` in die GHCR **gepusht**.
3. **Freigabe (nur freigegebene Container verwenden):**
   Nur ein Tag löst einen Push aus. Ein normaler Push auf `main` erzeugt **kein**
   neues freigegebenes Image. So kann keine ungetestete/ungetaggte Version in die
   Pipeline gelangen.
4. **Auto-PR:** Nach einem erfolgreichen Release erstellt der Workflow automatisch
   einen Pull Request, der die CI-Workflows auf die neue Version `vX.Y.Z` umstellt.
   Erst der **Merge** dieses PR (= Freigabe durch Review) übernimmt die neue Version.
5. **Neueste Version überall:** Nach dem Merge nutzen alle CI-Jobs die neue Version.
   Lokal baut der DevContainer denselben `.devcontainer/Dockerfile`, ist also auf
   demselben Stand.

### Neues Release erstellen

```
git tag v1.0.1
git push origin v1.0.1
```

Danach: Auto-PR abwarten und mergen.
