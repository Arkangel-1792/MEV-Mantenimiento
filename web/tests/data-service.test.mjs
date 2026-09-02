import test from 'node:test';
import assert from 'node:assert/strict';
import { DataService } from '../src/data-service.js';
import { assetPositionCount, COLLECTIONS } from '../src/core.js';

const memory = new Map();
globalThis.localStorage = {
  getItem: key => memory.has(key) ? memory.get(key) : null,
  setItem: (key, value) => memory.set(key, String(value)),
  removeItem: key => memory.delete(key)
};
Object.defineProperty(globalThis, 'navigator', { value: { onLine: true }, configurable: true });

test('el modo demostración guarda, envía y aprueba un registro', async () => {
  memory.clear();
  const service = new DataService();
  service.localAssets = [{ codigo: 'EPRUE0001', cantidadPosiciones: 4 }];
  await service.enterDemo('PLANIFICADOR');

  const created = await service.saveRecord(COLLECTIONS.maintenance, {
    codigoActivo: 'EPRUE0001', tipoServicio: 'CORRECTIVO', kilometraje: 100,
    horometro: null, accionEjecutada: 'Prueba funcional', observaciones: '',
    ordenTrabajo: 'OT-1', numeroPedido: '', estadoRegistro: 'BORRADOR'
  });
  assert.ok(created.id);

  await service.saveRecord(COLLECTIONS.maintenance, {
    codigoActivo: 'EPRUE0001', tipoServicio: 'CORRECTIVO', kilometraje: 101,
    horometro: null, accionEjecutada: 'Prueba funcional terminada', observaciones: '',
    ordenTrabajo: 'OT-1', numeroPedido: '', estadoRegistro: 'ENVIADO'
  }, { id: created.id });

  let sent = await service.listRecords({ collectionName: COLLECTIONS.maintenance, statuses: ['ENVIADO'] });
  assert.ok(sent.some(record => record.id === created.id));

  await service.reviewRecord(COLLECTIONS.maintenance, created.id, 'APROBADO');
  const approved = await service.listRecords({ collectionName: COLLECTIONS.maintenance, statuses: ['APROBADO'] });
  assert.ok(approved.some(record => record.id === created.id));
});

test('una devolución exige motivo', async () => {
  memory.clear();
  const service = new DataService();
  await service.enterDemo('PLANIFICADOR');
  const record = (await service.listRecords({ statuses: ['ENVIADO'] }))[0];
  await assert.rejects(() => service.reviewRecord(record._collection, record.id, 'DEVUELTO', ''), /motivo/i);
});

test('el planificador crea y administra usuarios en modo demostración', async () => {
  memory.clear();
  const service = new DataService();
  await service.enterDemo('PLANIFICADOR');
  const created = await service.createUser({
    nombres: 'Ana', apellidos: 'Prueba', email: 'ana.prueba@mev.local', cargo: 'Vulcanizadora',
    rol: 'VULCANIZADOR', estadoUsuario: 'ACTIVO'
  });
  assert.ok(created.uid);
  assert.ok((await service.listUsers()).some(item => item.email === 'ana.prueba@mev.local'));
  await service.updateUser(created.id, { rol: 'TECNICO_MECANICO', estadoUsuario: 'INACTIVO' });
  const updated = (await service.listUsers()).find(item => item.id === created.id);
  assert.equal(updated.rol, 'TECNICO_MECANICO');
  assert.equal(updated.estadoUsuario, 'INACTIVO');
});

test('cambiar posiciones del activo actualiza la configuración usada por llantas', async () => {
  memory.clear();
  const service = new DataService();
  service.localAssets = [{ codigo: 'EPRUE0002', cantidadPosiciones: 4, status: 'OPERATIVA' }];
  await service.enterDemo('PLANIFICADOR');
  const asset = service.assets[0];
  await service.updateAsset(asset, {
    ubicacionActual: 'LOJA', status: 'OPERATIVA', intervalo: 500, cantidadPosiciones: 12,
    permitePreventivo: true, permiteCorrectivo: true, permiteTomaHuella: true, permiteIntervencionLlanta: true
  });
  assert.equal(assetPositionCount(service.assets[0]), 12);
});
