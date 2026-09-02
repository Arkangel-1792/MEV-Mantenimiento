const CACHE_NAME = 'mev-web-v2.2.0';
const LOCAL_FILES = [
  '/', '/index.html', '/manifest.webmanifest',
  '/icon.svg', '/icon-192.png', '/icon-512.png',
  '/inventario_activos_firestore.json'
];

self.addEventListener('install', event => {
  event.waitUntil(caches.open(CACHE_NAME).then(cache => cache.addAll(LOCAL_FILES)));
  self.skipWaiting();
});

self.addEventListener('activate', event => {
  event.waitUntil(caches.keys().then(keys => Promise.all(keys.filter(key => key !== CACHE_NAME).map(key => caches.delete(key)))));
  self.clients.claim();
});

self.addEventListener('fetch', event => {
  const url = new URL(event.request.url);
  if (event.request.method !== 'GET' || url.origin !== self.location.origin) return;
  event.respondWith(fetch(event.request)
    .then(response => {
      if (response.ok) caches.open(CACHE_NAME).then(cache => cache.put(event.request, response.clone()));
      return response;
    })
    .catch(() => caches.match(event.request).then(cached => cached || caches.match('/index.html'))));
});
