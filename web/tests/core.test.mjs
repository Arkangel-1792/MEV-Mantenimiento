import test from 'node:test';
import assert from 'node:assert/strict';
import {
  COLLECTIONS,
  ROLE_MENUS,
  ROLES,
  assetPositionCount,
  calculateSummary,
  filterRecords,
  parseVoiceCommand
} from '../src/core.js';

test('existen los ocho roles y cada uno tiene menú de inicio', () => {
  assert.equal(ROLES.length, 8);
  for (const role of ROLES) assert.equal(ROLE_MENUS[role][0], 'Inicio');
});

test('las posiciones configuradas se respetan y se limitan a doce', () => {
  assert.equal(assetPositionCount({ cantidadPosiciones: 10 }), 10);
  assert.equal(assetPositionCount({ cantidadPosiciones: 20 }), 12);
  assert.equal(assetPositionCount({ subtipo: 'CAMIONETA' }), 4);
  assert.equal(assetPositionCount({ subtipo: 'VOLQUETA' }), 10);
});

test('el resumen consolida mantenimiento, huellas críticas e intervenciones', () => {
  const records = [
    { _collection: COLLECTIONS.maintenance, tipoServicio: 'PREVENTIVO', estadoRegistro: 'APROBADO' },
    { _collection: COLLECTIONS.maintenance, tipoServicio: 'CORRECTIVO', estadoRegistro: 'ENVIADO' },
    { _collection: COLLECTIONS.tread, huellas: [9, 6, 5.8, null], estadoRegistro: 'APROBADO' },
    { _collection: COLLECTIONS.tire, tipoIntervencion: 'CAMBIO', estadoRegistro: 'DEVUELTO' }
  ];
  const summary = calculateSummary(records);
  assert.equal(summary.total, 4);
  assert.equal(summary.preventivos, 1);
  assert.equal(summary.correctivos, 1);
  assert.equal(summary.huellasCriticas, 2);
  assert.equal(summary.cambios, 1);
  assert.equal(summary.enviados, 1);
});

test('los filtros combinan activo, estado y tipo', () => {
  const records = [
    { _collection: COLLECTIONS.maintenance, codigoActivo: 'EVOLQ0087', estadoRegistro: 'APROBADO', fechaCreacion: '2026-08-10T12:00:00Z' },
    { _collection: COLLECTIONS.tire, codigoActivo: 'ECAMI0041', estadoRegistro: 'ENVIADO', fechaCreacion: '2026-08-11T12:00:00Z' }
  ];
  assert.equal(filterRecords(records, { activo: 'volq', estado: 'APROBADO' }).length, 1);
  assert.equal(filterRecords(records, { tipo: COLLECTIONS.tire }).length, 1);
});

test('la voz interpreta campos principales de mantenimiento', () => {
  const command = parseVoiceCommand('Activo EVOLQ 87, servicio preventivo, kilometraje 84520, acción cambio de aceite y filtros, orden OT 184', 'maintenance');
  assert.equal(command.fields.codigoActivo, 'EVOLQ87');
  assert.equal(command.fields.tipoServicio, 'PREVENTIVO');
  assert.equal(command.fields.kilometraje, 84520);
  assert.match(command.fields.accionEjecutada, /cambio de aceite/);
});

test('la voz interpreta posiciones de huella', () => {
  const command = parseVoiceCommand('Activo EVOLQ 87, posición 1 huella 9.2, posición 2 huella 5.8, estado regular', 'tread');
  assert.equal(command.positions.P1, 9.2);
  assert.equal(command.positions.P2, 5.8);
  assert.equal(command.fields.estadoGeneral, 'REGULAR');
});

test('la voz separa varios campos y reconoce kilometraje y horómetro sin pausas obligatorias', () => {
  const command = parseVoiceCommand('Activo EVOLQ 87 servicio preventivo kilometraje ochenta y cuatro mil quinientos veinte horómetro seis mil trescientos cuarenta y uno acción cambio de aceite y filtros observaciones unidad operativa orden OT 184 pedido 791', 'maintenance');
  assert.equal(command.fields.codigoActivo, 'EVOLQ87');
  assert.equal(command.fields.kilometraje, 84520);
  assert.equal(command.fields.horometro, 6341);
  assert.equal(command.fields.accionEjecutada, 'cambio de aceite y filtros');
  assert.equal(command.fields.observaciones, 'unidad operativa');
  assert.equal(command.fields.ordenTrabajo, 'OT184');
  assert.equal(command.fields.numeroPedido, '791');
});

test('la voz reconoce posiciones y decimales expresados con palabras', () => {
  const command = parseVoiceCommand('Activo EVOLQ 87 proyecto Loja posición uno huella nueve punto dos posición dos huella cinco punto ocho estado regular', 'tread');
  assert.equal(command.positions.P1, 9.2);
  assert.equal(command.positions.P2, 5.8);
});

test('la voz separa los campos de una intervención de llanta', () => {
  const command = parseVoiceCommand('Activo EVOLQ 87 proyecto Loja cambio posición siete huella cinco punto ocho marca Bridgestone medida 12R22.5 serie ABC 99 motivo desgaste irregular observaciones rotación completada técnico Juan Pérez', 'tire');
  assert.equal(command.fields.proyecto, 'LOJA');
  assert.equal(command.fields.tipoIntervencion, 'CAMBIO');
  assert.equal(command.fields.posicion, 'P7');
  assert.equal(command.fields.huella, 5.8);
  assert.equal(command.fields.medidaLlanta, '12R22.5');
  assert.equal(command.fields.serieLlanta, 'ABC99');
});
