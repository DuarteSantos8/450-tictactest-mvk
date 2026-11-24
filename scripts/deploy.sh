#!/bin/bash
# Deployt einen Teil eines Releases auf die EC2-Instanz (per SSM, ohne SSH) und prueft danach die laufende Version.
# Aufruf: scripts/deploy.sh backend|frontend VERSION
# Braucht: AWS-Credentials (Umgebungsvariablen), INSTANCE_ID und APP_URL (z.B. http://1.2.3.4)
set -euo pipefail
PART="$1"
VERSION="$2"

# Auf der Instanz: tictactest-deploy (aus infra/setup.sh). Beim Backend zusaetzlich pruefen, ob das JAR startet.
COMMANDS="\"tictactest-deploy $PART $VERSION\""
if [ "$PART" = backend ]; then
  COMMANDS="$COMMANDS,\"echo | timeout 20 java -jar /opt/tictactest/tictactest.jar | grep -q 'where to put'\""
fi

COMMAND_ID=$(aws ssm send-command --instance-ids "$INSTANCE_ID" --document-name AWS-RunShellScript \
  --parameters "commands=[$COMMANDS]" --query Command.CommandId --output text)
# wait meldet einen Fehler, wenn der Befehl fehlschlaegt. Das Ergebnis holen wir trotzdem ab.
aws ssm wait command-executed --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" || true
aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" \
  --query '[Status,StandardOutputContent,StandardErrorContent]' --output text
STATUS=$(aws ssm get-command-invocation --command-id "$COMMAND_ID" --instance-id "$INSTANCE_ID" --query Status --output text)
if [ "$STATUS" != Success ]; then
  echo "::error::Deployment $PART $VERSION auf der Instanz fehlgeschlagen ($STATUS)"
  exit 1
fi

# Von aussen pruefen, dass genau diese Version ausgeliefert wird (max. 60 Sekunden warten)
if [ "$PART" = backend ]; then
  URL="$APP_URL/backend/version.txt"
else
  URL="$APP_URL/version.txt"
fi
for i in $(seq 1 12); do
  RUNNING=$(curl -fsS "$URL" || true)
  if [ "$RUNNING" = "$VERSION" ]; then
    echo "$PART $VERSION laeuft ($URL)"
    exit 0
  fi
  sleep 5
done
echo "::error::$URL meldet '$RUNNING' statt '$VERSION'"
exit 1
