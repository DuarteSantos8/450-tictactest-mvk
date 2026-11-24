#!/bin/bash
# Einrichtung beim ersten Start der Instanz (user_data, laeuft einmal als root).
# Installiert nur die Laufzeitumgebung. Welche App-Version laeuft, entscheidet das Deployment (tictactest-deploy).
set -euxo pipefail

# Java 25 (Amazon Corretto), nginx und unzip (Frontend-Archiv entpacken, Version aus dem JAR lesen)
dnf install -y java-25-amazon-corretto-headless nginx unzip

# ttyd ist nicht im Paket-Repository: feste Version vom GitHub-Release, Pruefsumme kontrollieren
curl -fsSL -o /usr/local/bin/ttyd https://github.com/tsl0922/ttyd/releases/download/${ttyd_version}/ttyd.x86_64
echo "${ttyd_sha256}  /usr/local/bin/ttyd" | sha256sum -c
chmod 755 /usr/local/bin/ttyd

# Eigener Benutzer ohne Login-Shell, damit das Spiel nicht als root laeuft
useradd --system --no-create-home --shell /sbin/nologin tictactest

# /opt/tictactest  = Backend (JAR + version.txt) und config.js (gehoert zur Infrastruktur)
# /var/www/tictactest = Frontend aus dem Release-Archiv
mkdir -p /opt/tictactest /var/www/tictactest
chmod 755 /opt/tictactest /var/www/tictactest

# config.js: Frontend und Web-Konsole laufen auf demselben Server, darum reichen relative Pfade.
# Die Datei liegt ausserhalb vom Frontend-Ordner, ein Frontend-Deployment kann sie nicht ueberschreiben.
cat > /opt/tictactest/config.js <<'EOF'
// Von der Infrastruktur geschrieben (infra/setup.sh), nicht Teil vom Frontend-Release
window.TICTACTEST_CONFIG = {
  terminalUrl: '/tty/',
  versionUrl: '/backend/version.txt'
};
EOF

# Dienst fuer die Web-Konsole: startet beim Booten, laeuft als tictactest, startet nach Absturz neu.
# ttyd startet NUR das JAR (keine Shell), hoechstens 5 Spiele gleichzeitig, wenig Heap pro JVM.
cat > /etc/systemd/system/ttyd.service <<'EOF'
[Unit]
Description=TicTacTest Web-Konsole (ttyd)
After=network.target

[Service]
User=tictactest
ExecStart=/usr/local/bin/ttyd -W -m 5 -i 127.0.0.1 -p 7681 -b /tty /usr/bin/java -Xmx64m -jar /opt/tictactest/tictactest.jar
Restart=always

[Install]
WantedBy=multi-user.target
EOF

# nginx: liefert das Frontend aus, leitet /tty/ per WebSocket an ttyd weiter, /backend/version.txt = Versionsabfrage
cat > /etc/nginx/nginx.conf <<'EOF'
user nginx;
events {}
http {
    include mime.types;

    server {
        listen 80;
        root /var/www/tictactest;

        location = /config.js {
            root /opt/tictactest;
            add_header Cache-Control no-store;
        }

        location = /backend/version.txt {
            alias /opt/tictactest/version.txt;
            default_type text/plain;
            add_header Cache-Control no-store;
            add_header Access-Control-Allow-Origin *;
        }

        location /tty/ {
            proxy_pass http://127.0.0.1:7681;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_read_timeout 1h;
        }
    }
}
EOF

# Deploy-Skript, wird per SSM aufgerufen:
#   tictactest-deploy backend 1.1.0   -> JAR holen, Pruefsumme pruefen, version.txt schreiben, Dienst neu starten
#   tictactest-deploy frontend 1.1.0  -> Archiv holen, Pruefsumme pruefen, Frontend-Ordner komplett ersetzen
cat > /usr/local/bin/tictactest-deploy <<'EOF'
#!/bin/bash
set -euo pipefail
PART="$1"
VERSION="$2"
URL="https://github.com/${repo}/releases/download/v$VERSION"
cd "$(mktemp -d)"

if [ "$PART" = backend ]; then
  FILE="tictactest-$VERSION.jar"
  curl -fsSLO "$URL/$FILE"
  curl -fsSLO "$URL/$FILE.sha256"
  sha256sum -c "$FILE.sha256"
  install -m 644 "$FILE" /opt/tictactest/tictactest.jar
  # Version aus dem Manifest im JAR, nicht von Hand
  unzip -p /opt/tictactest/tictactest.jar META-INF/MANIFEST.MF | grep Implementation-Version | cut -d' ' -f2 | tr -d '\r' > /opt/tictactest/version.txt
  systemctl restart ttyd
elif [ "$PART" = frontend ]; then
  FILE="tictactest-frontend-$VERSION.zip"
  curl -fsSLO "$URL/$FILE"
  curl -fsSLO "$URL/$FILE.sha256"
  sha256sum -c "$FILE.sha256"
  # alter Inhalt wird ganz ersetzt, Dateien die es nicht mehr gibt verschwinden
  rm -rf /var/www/tictactest.new
  unzip -q "$FILE" -d /var/www/tictactest.new
  chmod -R a+rX /var/www/tictactest.new
  rm -rf /var/www/tictactest
  mv /var/www/tictactest.new /var/www/tictactest
else
  echo "Aufruf: tictactest-deploy backend|frontend VERSION" >&2
  exit 1
fi
echo "$PART $VERSION deployt"
EOF
chmod 755 /usr/local/bin/tictactest-deploy

systemctl daemon-reload
systemctl enable --now ttyd nginx
