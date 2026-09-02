import test from 'node:test';
import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');

test('el catálogo local contiene los 162 activos esperados', async () => {
  const content = await readFile(path.join(root, 'public', 'inventario_activos_firestore.json'), 'utf8');
  const assets = JSON.parse(content);
  assert.equal(assets.length, 162);
  assert.ok(assets.every(asset => asset.codigo));
});

test('la página enlaza estilos, aplicación y manifiesto', async () => {
  const html = await readFile(path.join(root, 'index.html'), 'utf8');
  assert.match(html, /src\/styles\.css/);
  assert.match(html, /src\/main\.js/);
  assert.match(html, /manifest\.webmanifest/);
});

test('el proyecto incluye inicio rápido y modo sin conexión', async () => {
  const starter = await readFile(path.join(root, 'INICIAR_WEB.bat'), 'utf8');
  const worker = await readFile(path.join(root, 'public', 'service-worker.js'), 'utf8');
  assert.match(starter, /node servidor\.js/);
  assert.match(worker, /inventario_activos_firestore\.json/);
});

test('la interfaz y las reglas incluyen creación administrada de usuarios', async () => {
  const main = await readFile(path.join(root, 'src', 'main.js'), 'utf8');
  const service = await readFile(path.join(root, 'src', 'data-service.js'), 'utf8');
  const rules = await readFile(path.join(root, 'firebase', 'firestore.rules'), 'utf8');
  assert.match(main, /Crear usuario/);
  assert.match(service, /createUserWithEmailAndPassword/);
  assert.match(rules, /allow create, update: if roleIn\(\['PLANIFICADOR'\]\)/);
});

test('la compilación web está preparada para publicación en línea', async () => {
  const packageJson = JSON.parse(await readFile(path.join(root, 'package.json'), 'utf8'));
  const hosting = JSON.parse(await readFile(path.join(root, '.openai', 'hosting.json'), 'utf8'));
  const worker = await readFile(path.join(root, 'worker', 'index.js'), 'utf8');
  const main = await readFile(path.join(root, 'src', 'main.js'), 'utf8');
  assert.equal(packageJson.scripts.build, 'vite build');
  assert.match(hosting.project_id, /^appgprj_/);
  assert.match(worker, /\/health/);
  assert.match(main, /Actualizar datos/);
});
