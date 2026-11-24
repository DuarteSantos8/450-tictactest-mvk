# Kleiner Webserver fuer lokal: liefert version.txt aus dem aktuellen Ordner aus.
# Der Header Access-Control-Allow-Origin erlaubt dem Frontend (anderer Port = andere Origin),
# die Datei per fetch() zu lesen (CORS). In der Cloud macht das spaeter nginx.
# Start: python3 version-server.py 7682
import sys
from http.server import HTTPServer, SimpleHTTPRequestHandler


class CorsHandler(SimpleHTTPRequestHandler):
    def end_headers(self):
        self.send_header('Access-Control-Allow-Origin', '*')
        super().end_headers()


port = int(sys.argv[1]) if len(sys.argv) > 1 else 7682
HTTPServer(('127.0.0.1', port), CorsHandler).serve_forever()
