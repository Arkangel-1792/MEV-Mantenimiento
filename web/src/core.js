export const APP_VERSION = '2.2.0';

export const COLLECTIONS = Object.freeze({
  maintenance: 'registros_mantenimiento',
  tread: 'tomas_huella',
  tire: 'intervenciones_llanta'
});

export const RECORD_LABELS = Object.freeze({
  [COLLECTIONS.maintenance]: 'Mantenimiento',
  [COLLECTIONS.tread]: 'Toma de huella',
  [COLLECTIONS.tire]: 'Intervención de llanta'
});

export const ROLES = Object.freeze([
  'TECNICO_MECANICO',
  'VULCANIZADOR',
  'SUPERVISOR_MANTENIMIENTO',
  'ANALISTA_MANTENIMIENTO',
  'ASISTENTE_PLANIFICACION',
  'PLANIFICADOR',
  'JEFE_OPERACIONES',
  'GERENTE_GENERAL'
]);

export const ROLE_LABELS = Object.freeze({
  TECNICO_MECANICO: 'Técnico mecánico',
  VULCANIZADOR: 'Vulcanizador',
  SUPERVISOR_MANTENIMIENTO: 'Supervisor de mantenimiento',
  ANALISTA_MANTENIMIENTO: 'Analista de mantenimiento',
  ASISTENTE_PLANIFICACION: 'Asistente de planificación',
  PLANIFICADOR: 'Planificador',
  JEFE_OPERACIONES: 'Jefe de operaciones',
  GERENTE_GENERAL: 'Gerente general'
});

export const ROLE_MENUS = Object.freeze({
  TECNICO_MECANICO: ['Inicio', 'Nuevo mantenimiento', 'Mis borradores', 'Mi historial'],
  VULCANIZADOR: ['Inicio', 'Toma general de huella', 'Intervención de llanta', 'Mis borradores', 'Mi historial'],
  SUPERVISOR_MANTENIMIENTO: ['Inicio', 'Nuevo mantenimiento', 'Vulcanización', 'Revisión de registros', 'Reportes'],
  ANALISTA_MANTENIMIENTO: ['Inicio', 'Nuevo mantenimiento', 'Vulcanización', 'Revisión de registros', 'Reportes', 'Matriz base'],
  ASISTENTE_PLANIFICACION: ['Inicio', 'Nuevo mantenimiento', 'Mis borradores', 'Vulcanización', 'Revisión de registros', 'Reportes', 'Matriz base'],
  PLANIFICADOR: ['Inicio', 'Nuevo mantenimiento', 'Toma general de huella', 'Intervención de llanta', 'Mis borradores', 'Mi historial', 'Revisión de registros', 'Reportes', 'Matriz base', 'Usuarios'],
  JEFE_OPERACIONES: ['Inicio', 'Panel gerencial', 'Reportes', 'Matriz base'],
  GERENTE_GENERAL: ['Inicio', 'Panel gerencial', 'Reportes', 'Matriz base']
});

export const PAGE_META = Object.freeze({
  Inicio: ['Panel de inicio', 'Accesos y actividad reciente del sistema.'],
  'Nuevo mantenimiento': ['Nuevo mantenimiento', 'Registra una actividad preventiva o correctiva.'],
  Vulcanización: ['Vulcanización', 'Gestión integral de llantas y medición de huellas.'],
  'Toma general de huella': ['Toma general de huella', 'Registra las mediciones por posición de cada activo.'],
  'Intervención de llanta': ['Intervención de llanta', 'Registra cambios, rotaciones, reparaciones, montajes, desmontajes o bajas.'],
  'Mis borradores': ['Mis borradores y correcciones', 'Edita registros en BORRADOR o DEVUELTO antes de enviarlos.'],
  'Mi historial': ['Mi historial', 'Consulta los registros enviados, aprobados y devueltos.'],
  'Revisión de registros': ['Revisión de registros', 'Aprueba o devuelve registros operativos con su observación.'],
  Reportes: ['Reportes', 'Consulta, filtra y exporta la información consolidada.'],
  'Panel gerencial': ['Panel gerencial', 'Indicadores ejecutivos de mantenimiento y vulcanización.'],
  'Matriz base': ['Matriz base', 'Catálogo maestro y configuración de los activos.'],
  Usuarios: ['Usuarios y roles', 'Consulta y administra los permisos de acceso.']
});

export const STATUS_ORDER = ['BORRADOR', 'ENVIADO', 'APROBADO', 'DEVUELTO'];

export function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>'"]/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', "'": '&#039;', '"': '&quot;'
  })[character]);
}

export function normalizeText(value) {
  return String(value ?? '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9./_-]+/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

export function normalizeUpper(value) {
  return String(value ?? '').trim().toUpperCase();
}

export function numberOrNull(value) {
  if (value === '' || value === null || value === undefined) return null;
  const parsed = Number(String(value).trim().replace(',', '.'));
  return Number.isFinite(parsed) ? parsed : null;
}

export function timestampToDate(value) {
  if (!value) return null;
  if (typeof value.toDate === 'function') return value.toDate();
  if (typeof value.seconds === 'number') return new Date(value.seconds * 1000);
  const date = value instanceof Date ? value : new Date(value);
  return Number.isNaN(date.getTime()) ? null : date;
}

export function formatDate(value, includeTime = true) {
  const date = timestampToDate(value);
  if (!date) return 'Sin fecha';
  const options = includeTime
    ? { dateStyle: 'short', timeStyle: 'short' }
    : { dateStyle: 'short' };
  return new Intl.DateTimeFormat('es-EC', options).format(date);
}

export function formatNumber(value, suffix = '') {
  const number = numberOrNull(value);
  if (number === null) return 'Sin registro';
  const rendered = new Intl.NumberFormat('es-EC', { maximumFractionDigits: 2 }).format(number);
  return suffix ? `${rendered} ${suffix}` : rendered;
}

export function roleLabel(role) {
  return ROLE_LABELS[role] || String(role ?? '').replaceAll('_', ' ');
}

export function recordLabel(collectionName) {
  return RECORD_LABELS[collectionName] || 'Registro';
}

export function recordDate(record) {
  return record.fechaActualizacion || record.fechaCreacion || record.fechaEnvio || record.fechaRevision;
}

export function sortRecords(records) {
  return [...records].sort((a, b) => {
    const right = timestampToDate(recordDate(b))?.getTime() || 0;
    const left = timestampToDate(recordDate(a))?.getTime() || 0;
    return right - left;
  });
}

export function assetPositionCount(asset) {
  const configured = Number(asset?.cantidadPosiciones);
  if (Number.isFinite(configured) && configured > 0) return Math.min(12, Math.max(0, configured));
  const subtype = normalizeText(asset?.subtipo || asset?.tipo);
  if (/shacman|volqueta 4/.test(subtype)) return 12;
  if (/volqueta/.test(subtype)) return 10;
  if (/camioneta|retroexcavadora|minicargadora/.test(subtype)) return 4;
  if (/camion|cabezal|tanquero/.test(subtype)) return 6;
  if (/motoniveladora/.test(subtype)) return 6;
  if (/rodillo/.test(subtype)) return 2;
  return 0;
}

export function assetAllows(asset, capability) {
  if (!asset) return false;
  const explicit = asset[capability];
  if (typeof explicit === 'boolean') return explicit;
  if (capability === 'permiteTomaHuella' || capability === 'permiteIntervencionLlanta') {
    return asset.aplicaVulcanizacion === true || assetPositionCount(asset) > 0;
  }
  return true;
}

export function getTreadValues(record) {
  if (Array.isArray(record?.huellas)) {
    const values = record.huellas.map(numberOrNull);
    if (values.some(value => value !== null)) return values.slice(0, 12);
  }
  return Array.from({ length: 12 }, (_, index) => numberOrNull(record?.[`P${index + 1}`]));
}

export function readingLabel(record) {
  const kilometer = numberOrNull(record?.kilometraje);
  const hour = numberOrNull(record?.horometro);
  if (kilometer !== null && hour !== null) return `${formatNumber(kilometer)} km · ${formatNumber(hour)} h`;
  if (kilometer !== null) return `${formatNumber(kilometer)} km`;
  if (hour !== null) return `${formatNumber(hour)} h`;
  return 'Sin lectura';
}

export function calculateSummary(records) {
  const maintenance = records.filter(record => record._collection === COLLECTIONS.maintenance);
  const tread = records.filter(record => record._collection === COLLECTIONS.tread);
  const tire = records.filter(record => record._collection === COLLECTIONS.tire);
  const statusCount = status => records.filter(record => normalizeUpper(record.estadoRegistro) === status).length;
  const interventionCount = type => tire.filter(record => normalizeUpper(record.tipoIntervencion) === type).length;
  const criticalTreads = tread.reduce((total, record) => total + getTreadValues(record)
    .filter(value => value !== null && value > 0 && value <= 6).length, 0);
  return {
    total: records.length,
    totalMantenimientos: maintenance.length,
    preventivos: maintenance.filter(record => normalizeUpper(record.tipoServicio) === 'PREVENTIVO').length,
    correctivos: maintenance.filter(record => normalizeUpper(record.tipoServicio) === 'CORRECTIVO').length,
    totalHuellas: tread.length,
    huellasCriticas: criticalTreads,
    totalIntervenciones: tire.length,
    cambios: interventionCount('CAMBIO'),
    rotaciones: interventionCount('ROTACION'),
    reparaciones: interventionCount('REPARACION'),
    montajes: interventionCount('MONTAJE'),
    desmontajes: interventionCount('DESMONTAJE'),
    bajas: interventionCount('BAJA'),
    borradores: statusCount('BORRADOR'),
    enviados: statusCount('ENVIADO'),
    aprobados: statusCount('APROBADO'),
    devueltos: statusCount('DEVUELTO')
  };
}

export function filterRecords(records, filters = {}) {
  const asset = normalizeText(filters.activo);
  const project = normalizeText(filters.proyecto);
  const technician = normalizeText(filters.tecnico);
  const status = normalizeUpper(filters.estado);
  const type = String(filters.tipo || '');
  const from = filters.fechaDesde ? new Date(`${filters.fechaDesde}T00:00:00`).getTime() : null;
  const to = filters.fechaHasta ? new Date(`${filters.fechaHasta}T23:59:59.999`).getTime() : null;
  return records.filter(record => {
    if (asset && !normalizeText(record.codigoActivo).includes(asset)) return false;
    if (project && !normalizeText(record.proyecto).includes(project)) return false;
    const technicianText = normalizeText(`${record.nombreTecnico || ''} ${record.emailUsuario || ''}`);
    if (technician && !technicianText.includes(technician)) return false;
    if (status && normalizeUpper(record.estadoRegistro) !== status) return false;
    if (type && record._collection !== type) return false;
    const time = timestampToDate(recordDate(record))?.getTime();
    if (from !== null && (!time || time < from)) return false;
    if (to !== null && (!time || time > to)) return false;
    return true;
  });
}

const NUMBER_WORDS = Object.freeze({
  cero: 0, un: 1, uno: 1, una: 1, dos: 2, tres: 3, cuatro: 4, cinco: 5,
  seis: 6, siete: 7, ocho: 8, nueve: 9, diez: 10, once: 11, doce: 12,
  trece: 13, catorce: 14, quince: 15, dieciseis: 16, diecisiete: 17,
  dieciocho: 18, diecinueve: 19, veinte: 20, veintiuno: 21, veintidos: 22,
  veintitres: 23, veinticuatro: 24, veinticinco: 25, veintiseis: 26,
  veintisiete: 27, veintiocho: 28, veintinueve: 29, treinta: 30, cuarenta: 40,
  cincuenta: 50, sesenta: 60, setenta: 70, ochenta: 80, noventa: 90,
  cien: 100, ciento: 100, doscientos: 200, trescientos: 300, cuatrocientos: 400,
  quinientos: 500, seiscientos: 600, setecientos: 700, ochocientos: 800,
  novecientos: 900, mil: 1000
});

function wordsToNumber(value) {
  const direct = numberOrNull(value);
  if (direct !== null) return direct;
  const tokens = normalizeText(value).split(' ').filter(Boolean);
  let total = 0;
  let current = 0;
  let found = false;
  for (const token of tokens) {
    if (!(token in NUMBER_WORDS)) continue;
    found = true;
    const number = NUMBER_WORDS[token];
    if (number === 1000) {
      current = (current || 1) * 1000;
      total += current;
      current = 0;
    } else {
      current += number;
    }
  }
  return found ? total + current : null;
}

function digitSequenceToNumber(value) {
  const match = String(value ?? '').match(/[0-9]+(?:[\s.,][0-9]+)*/);
  if (!match) return null;
  const compact = match[0].trim().replace(/\s+/g, '');
  if (!compact) return null;
  const groups = compact.split(/[.,]/);
  if (groups.length === 1) return numberOrNull(groups[0]);
  if (groups.slice(1).every(group => group.length === 3)) return numberOrNull(groups.join(''));
  const decimal = `${groups.slice(0, -1).join('')}.${groups.at(-1)}`;
  return numberOrNull(decimal);
}

function spokenNumber(value) {
  const normalized = normalizeText(value);
  if (!normalized) return null;
  const decimalParts = normalized.split(/\b(?:punto|coma)\b/, 2);
  if (decimalParts.length === 2) {
    const whole = digitSequenceToNumber(decimalParts[0]) ?? wordsToNumber(decimalParts[0]);
    const fractionTokens = decimalParts[1].split(' ').filter(Boolean);
    const fraction = fractionTokens.map(token => {
      if (/^[0-9]+$/.test(token)) return token;
      const number = NUMBER_WORDS[token];
      return Number.isInteger(number) && number >= 0 && number <= 9 ? String(number) : '';
    }).join('');
    if (whole !== null && fraction) return Number(`${whole}.${fraction}`);
  }
  return digitSequenceToNumber(normalized) ?? wordsToNumber(normalized);
}

function capture(text, expression) {
  const match = text.match(expression);
  return match?.[1]?.trim() || '';
}

function extractAssetCode(normalized) {
  const labeled = capture(normalized, /(?:activo|unidad|equipo)\s+(?:codigo\s+)?([a-z]{1,12}\s*-?\s*\d{1,6})/i);
  const free = capture(normalized, /\b([a-z]{2,12}\s*-?\s*\d{2,6})\b/i);
  return (labeled || free).replace(/[\s-]+/g, '').toUpperCase();
}

const VOICE_LABELS = Object.freeze([
  ['codigoActivo', 'codigo\\s+(?:de(?:l)?\\s+)?(?:activo|unidad|equipo)|activo|unidad|equipo'],
  ['tipoServicio', 'tipo\\s+de\\s+(?:servicio|mantenimiento)|servicio'],
  ['kilometraje', 'lectura\\s+de\\s+kilometraje|kilometraje(?:\\s+actual)?|kilometros(?:\\s+recorridos)?|km'],
  ['horometro', 'lectura\\s+(?:de(?:l)?\\s+)?horometro|horometro(?:\\s+actual)?|horas\\s+(?:de\\s+)?(?:motor|trabajo)|horas'],
  ['accionEjecutada', 'accion(?:\\s+ejecutada)?|trabajo\\s+realizado|actividad(?:\\s+realizada)?'],
  ['observaciones', 'observaciones?|comentarios?'],
  ['ordenTrabajo', 'numero\\s+de\\s+orden(?:\\s+de\\s+trabajo)?|orden\\s+de\\s+trabajo|orden|ot'],
  ['numeroPedido', 'numero\\s+de\\s+pedido|pedido'],
  ['proyecto', 'proyecto|campamento|frente(?:\\s+de\\s+trabajo)?'],
  ['posicion', 'posicion(?:\\s+de\\s+llanta)?'],
  ['huella', 'huella(?:\\s+actual)?|profundidad'],
  ['marcaLlanta', 'marca(?:\\s+de\\s+llanta)?'],
  ['medidaLlanta', 'medida(?:\\s+de\\s+llanta)?'],
  ['serieLlanta', 'serie(?:\\s+de\\s+llanta)?|identificacion(?:\\s+de\\s+llanta)?'],
  ['motivo', 'motivo(?:\\s+de\\s+intervencion)?'],
  ['estadoGeneral', 'estado(?:\\s+general)?'],
  ['nombreTecnico', 'nombre\\s+(?:de(?:l)?\\s+)?(?:tecnico|vulcanizador)|tecnico|vulcanizador'],
  ['novedad', 'novedades?']
]);

function voiceSegments(text) {
  const groupedLabels = VOICE_LABELS.map(([, expression], index) => `(?<label${index}>${expression})`).join('|');
  const matches = [...text.matchAll(new RegExp(`\\b(?:${groupedLabels})\\b`, 'gi'))].filter(match => {
    const groupName = Object.keys(match.groups || {}).find(name => match.groups[name]);
    const labelIndex = Number(String(groupName || '').replace('label', ''));
    const key = VOICE_LABELS[labelIndex]?.[0] || '';
    if (key !== 'codigoActivo' || /^codigo\b/i.test(match[0])) return true;
    const followingText = text.slice((match.index || 0) + match[0].length);
    return /^\s+(?:codigo\s+)?[a-z]{1,12}\s*-?\s*\d{1,6}\b/i.test(followingText);
  });
  return matches.map((match, index) => {
    const groupName = Object.keys(match.groups || {}).find(name => match.groups[name]);
    const labelIndex = Number(String(groupName || '').replace('label', ''));
    const key = VOICE_LABELS[labelIndex]?.[0] || '';
    const nextIndex = matches[index + 1]?.index ?? text.length;
    const rawValue = text.slice((match.index || 0) + match[0].length, nextIndex);
    const value = rawValue
      .replace(/^(?:(?:es|de|del|numero|nro|no|codigo)\s+)+/i, '')
      .replace(/\s+(?:y|e)$/i, '')
      .trim();
    return { key, value, label: match[0], index: match.index || 0 };
  }).filter(segment => segment.key);
}

function segmentValue(segments, key) {
  return segments.find(segment => segment.key === key && segment.value)?.value || '';
}

function segmentEntry(segments, key) {
  return segments.find(segment => segment.key === key && segment.value) || null;
}

function parsePositionSegments(segments) {
  const positions = {};
  segments.forEach((segment, index) => {
    if (segment.key !== 'posicion') return;
    const position = spokenNumber(segment.value);
    if (!Number.isInteger(position) || position < 1 || position > 12) return;
    const nextPositionIndex = segments.findIndex((candidate, candidateIndex) => candidateIndex > index && candidate.key === 'posicion');
    const end = nextPositionIndex < 0 ? segments.length : nextPositionIndex;
    const treadSegment = segments.slice(index + 1, end).find(candidate => candidate.key === 'huella');
    const tread = spokenNumber(treadSegment?.value);
    if (tread !== null) positions[`P${position}`] = tread;
  });
  return positions;
}

export function parseVoiceCommand(rawText, formType) {
  const normalized = normalizeText(rawText);
  const segments = voiceSegments(normalized);
  const result = { transcript: String(rawText ?? '').trim(), fields: {}, positions: {} };
  const asset = extractAssetCode(normalized);
  if (asset) result.fields.codigoActivo = asset;
  const kilometer = spokenNumber(segmentValue(segments, 'kilometraje'));
  const hour = spokenNumber(segmentValue(segments, 'horometro'));
  if (kilometer !== null) result.fields.kilometraje = kilometer;
  if (hour !== null) result.fields.horometro = hour;

  if (formType === 'maintenance') {
    if (/\bpreventiv[oa]\b/.test(normalized)) result.fields.tipoServicio = 'PREVENTIVO';
    if (/\bcorrectiv[oa]\b/.test(normalized)) result.fields.tipoServicio = 'CORRECTIVO';
    const action = segmentValue(segments, 'accionEjecutada');
    const observation = segmentValue(segments, 'observaciones');
    const workOrderEntry = segmentEntry(segments, 'ordenTrabajo');
    const workOrder = workOrderEntry ? `${normalizeText(workOrderEntry.label) === 'ot' ? 'OT ' : ''}${workOrderEntry.value}` : '';
    const request = segmentValue(segments, 'numeroPedido');
    if (action) result.fields.accionEjecutada = action;
    if (observation) result.fields.observaciones = observation;
    if (workOrder) result.fields.ordenTrabajo = workOrder.replace(/\s+/g, '').toUpperCase();
    if (request) result.fields.numeroPedido = request.replace(/\s+/g, '').toUpperCase();
  }

  if (formType === 'tread' || formType === 'tire') {
    let project = segmentValue(segments, 'proyecto');
    if (formType === 'tire') project = project.replace(/\s+(?:cambio|rotacion|reparacion|montaje|desmontaje|baja)$/i, '').trim();
    const technician = segmentValue(segments, 'nombreTecnico');
    if (project) result.fields.proyecto = project.toUpperCase();
    if (technician) result.fields.nombreTecnico = technician.replace(/\b\w/g, letter => letter.toUpperCase());
  }

  if (formType === 'tread') {
    const stateValue = segmentValue(segments, 'estadoGeneral');
    const stateMatch = stateValue.match(/\b(bueno|regular|malo)\b/) || normalized.match(/estado(?: general)?\s+(bueno|regular|malo)/);
    if (stateMatch) result.fields.estadoGeneral = stateMatch[1].toUpperCase();
    const note = segmentValue(segments, 'novedad') || segmentValue(segments, 'observaciones');
    if (note) result.fields.novedad = note;
    Object.assign(result.positions, parsePositionSegments(segments));
    const positionExpression = /(?:posicion|posición|p)\s*([1-9]|1[0-2])\s*(?:huella|medida|es|de|:)?\s*([0-9]+(?:[.,][0-9]+)?)/gi;
    for (const match of normalized.matchAll(positionExpression)) {
      result.positions[`P${match[1]}`] = Number(match[2].replace(',', '.'));
    }
  }

  if (formType === 'tire') {
    const interventionMap = [
      ['CAMBIO', /\bcambio\b/], ['ROTACION', /\brotacion\b/], ['REPARACION', /\breparacion\b/],
      ['MONTAJE', /\bmontaje\b/], ['DESMONTAJE', /\bdesmontaje\b/], ['BAJA', /\bbaja\b/]
    ];
    const intervention = interventionMap.find(([, expression]) => expression.test(normalized));
    if (intervention) result.fields.tipoIntervencion = intervention[0];
    const position = spokenNumber(segmentValue(segments, 'posicion')) ?? numberOrNull(capture(normalized, /(?:posicion|posición)\s*(?:es|:)?\s*p?\s*([1-9]|1[0-2])/i));
    const treadValue = spokenNumber(segmentValue(segments, 'huella'));
    const brand = segmentValue(segments, 'marcaLlanta');
    const measure = segmentValue(segments, 'medidaLlanta');
    const serial = segmentValue(segments, 'serieLlanta');
    const reason = segmentValue(segments, 'motivo');
    const observations = segmentValue(segments, 'observaciones');
    if (Number.isInteger(position) && position >= 1 && position <= 12) result.fields.posicion = `P${position}`;
    if (treadValue !== null) result.fields.huella = treadValue;
    if (brand) result.fields.marcaLlanta = brand.toUpperCase();
    if (measure) result.fields.medidaLlanta = measure.toUpperCase();
    if (serial) result.fields.serieLlanta = serial.replace(/\s+/g, '').toUpperCase();
    if (reason) result.fields.motivo = reason;
    if (observations) result.fields.observaciones = observations;
  }

  return result;
}

export function recordSearchText(record) {
  return normalizeText([
    record.codigoActivo, record.proyecto, record.tipoServicio, record.tipoIntervencion,
    record.accionEjecutada, record.observaciones, record.novedad, record.motivo,
    record.nombreTecnico, record.emailUsuario, record.estadoRegistro
  ].filter(Boolean).join(' '));
}

export function validatePosition(position, asset) {
  const match = normalizeUpper(position).match(/^P([1-9]|1[0-2])$/);
  if (!match) return false;
  const count = assetPositionCount(asset);
  return count > 0 && Number(match[1]) <= count;
}
