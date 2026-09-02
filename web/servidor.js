import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.dirname(fileURLToPath(import.meta.url));
const port = Number(process.env.MEV_PORT || 5500);
const host = process.env.MEV_HOST || '127.0.0.1';
const mime = {
  '.html': 'text/html; charset=utf-8',
  '.js': 'text/javascript; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.webmanifest': 'application/manifest+json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};
const publicFiles = new Set([
  '/icon.svg',
  '/icon-192.png',
  '/icon-512.png',
  '/manifest.webmanifest',
  '/service-worker.js',
  '/inventario_activos_firestore.json'
]);

function resolveRequest(url = '/') {
  const pathname = decodeURIComponent(url.split('?')[0]);
  const normalized = pathname === '/' ? '/index.html' : pathname;
  const relativePath = publicFiles.has(normalized) ? `/public${normalized}` : normalized;
  const candidate = path.resolve(root, `.${relativePath}`);
  if (candidate !== root && !candidate.startsWith(`${root}${path.sep}`)) return null;
  return candidate;
}

const server = http.createServer((request, response) => {
  const file = resolveRequest(request.url);
  if (!file) {
    response.writeHead(403, { 'Content-Type': 'text/plain; charset=utf-8' });
    response.end('Acceso denegado');
    return;
  }
  fs.readFile(file, (error, data) => {
    if (error) {
      response.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
      response.end('Archivo no encontrado');
      return;
    }
    const cacheControl = path.basename(file) === 'service-worker.js' ? 'no-cache' : 'no-store';
    response.writeHead(200, {
      'Content-Type': mime[path.extname(file)] || 'application/octet-stream',
      'Cache-Control': cacheControl,
      'X-Content-Type-Options': 'nosniff',
      'Referrer-Policy': 'strict-origin-when-cross-origin'
    });
    response.end(data);
  });
});

server.listen(port, host, () => {
  console.log(`MEV Mantenimiento Web disponible en http://${host === '0.0.0.0' ? 'localhost' : host}:${port}`);
  console.log('Mantén esta ventana abierta mientras utilizas la aplicación.');
});

for (const signal of ['SIGINT', 'SIGTERM']) {
  process.on(signal, () => server.close(() => process.exit(0)));
}
