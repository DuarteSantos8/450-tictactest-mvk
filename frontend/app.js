// Die URLs kommen aus config.js (pro Umgebung verschieden), nicht aus diesem Code
const config = window.TICTACTEST_CONFIG;

// Text einer URL holen, bei Fehler einen Ersatztext anzeigen
async function loadText(url, fallback) {
  try {
    const response = await fetch(url, { cache: 'no-store' });
    if (!response.ok) {
      return fallback;
    }
    return (await response.text()).trim();
  } catch (e) {
    return fallback;
  }
}

async function start() {
  if (!config) {
    document.getElementById('backend-version').textContent = 'config.js fehlt';
    return;
  }
  document.getElementById('terminal').src = config.terminalUrl;
  document.getElementById('backend-version').textContent = await loadText(config.versionUrl, 'nicht erreichbar');
  // version.txt wird beim Release ins Frontend-Archiv geschrieben, lokal aus dem Repo gibt es sie nicht
  document.getElementById('frontend-version').textContent = await loadText('version.txt', 'dev');
}

start();
