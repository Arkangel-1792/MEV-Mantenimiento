import {
  APP_VERSION, COLLECTIONS, PAGE_META, RECORD_LABELS, ROLE_MENUS, ROLES,
  assetAllows, assetPositionCount, calculateSummary, escapeHtml, filterRecords,
  formatDate, formatNumber, getTreadValues, normalizeText, normalizeUpper,
  readingLabel, recordDate, recordLabel, roleLabel, validatePosition
} from './core.js';
import { DataService } from './data-service.js';
import { downloadExcel, printReport } from './export.js';
import { listenForCommand, speechSupported, voiceHelp } from './voice.js';

const app = document.querySelector('#app');
const service = new DataService();
const state = {
  session: null, page: 'Inicio', editing: null, records: [],
  reportRecords: [], filteredReportRecords: [], matrixPage: 1, modalReturnFocus: null
};

const ICONS = Object.freeze({
  Inicio: '⌂', 'Nuevo mantenimiento': '⚙', Vulcanización: '◉',
  'Toma general de huella': '◎', 'Intervención de llanta': '↻',
  'Mis borradores': '✎', 'Mi historial': '≡', 'Revisión de registros': '✓',
  Reportes: '▥', 'Panel gerencial': '▦', 'Matriz base': '⌘', Usuarios: '♙'
});
const REVIEW_ROLES = new Set(['SUPERVISOR_MANTENIMIENTO', 'ANALISTA_MANTENIMIENTO', 'ASISTENTE_PLANIFICACION', 'PLANIFICADOR']);

const profile = () => state.session?.profile || {};
const user = () => state.session?.user || {};
const fullName = (value = profile()) => `${value.nombres || ''} ${value.apellidos || ''}`.trim() || value.email || user().email || 'Usuario MEV';
const pageRoot = () => document.querySelector('#page');

function pageHeader(page = state.page, customSubtitle = '') {
  const [title, subtitle] = PAGE_META[page] || [page, ''];
  const refreshAction = service.mode === 'firebase'
    ? '<button class="btn btn-outline btn-small" type="button" data-refresh-page aria-label="Actualizar datos desde Firebase">↻ Actualizar datos</button>'
    : '';
  return `<div class="page-heading"><div><div class="eyebrow">MEV · CONTROL OPERATIVO</div><h1>${escapeHtml(title)}</h1><p>${escapeHtml(customSubtitle || subtitle)}</p></div><div class="heading-actions">${refreshAction}<span class="role-chip">${escapeHtml(roleLabel(profile().rol))}</span></div></div>`;
}

function loader(message = 'Cargando información…') {
  return `<div class="loading-card" role="status"><span class="spinner" aria-hidden="true"></span><span>${escapeHtml(message)}</span></div>`;
}

function emptyState(title, message, action = '') {
  return `<div class="empty-state"><div class="empty-icon">◇</div><h3>${escapeHtml(title)}</h3><p>${escapeHtml(message)}</p>${action}</div>`;
}

function toast(message, type = 'success', duration = 4500) {
  let region = document.querySelector('#toast-region');
  if (!region) {
    region = document.createElement('div');
    region.id = 'toast-region';
    region.className = 'toast-region';
    region.setAttribute('aria-live', 'polite');
    document.body.appendChild(region);
  }
  const item = document.createElement('div');
  item.className = `toast toast-${type}`;
  item.setAttribute('role', type === 'error' ? 'alert' : 'status');
  item.innerHTML = `<span>${type === 'error' ? '!' : type === 'warning' ? '△' : '✓'}</span><div>${escapeHtml(message)}</div><button aria-label="Cerrar aviso">×</button>`;
  item.querySelector('button').onclick = () => item.remove();
  region.appendChild(item);
  setTimeout(() => item.remove(), duration);
}

function setBusy(button, busy, label = 'Procesando…') {
  if (!button) return;
  if (busy) {
    button.dataset.originalText = button.textContent;
    button.disabled = true;
    button.textContent = label;
  } else {
    button.disabled = false;
    button.textContent = button.dataset.originalText || button.textContent;
  }
}

function openModal(title, content, { wide = false, onOpen = null } = {}) {
  state.modalReturnFocus = document.activeElement;
  let root = document.querySelector('#modal-root');
  if (!root) {
    root = document.createElement('div');
    root.id = 'modal-root';
    document.body.appendChild(root);
  }
  root.innerHTML = `<div class="modal-backdrop" data-modal-close></div><section class="modal ${wide ? 'modal-wide' : ''}" role="dialog" aria-modal="true" aria-labelledby="modal-title"><div class="modal-header"><h2 id="modal-title">${escapeHtml(title)}</h2><button class="icon-button" data-modal-close aria-label="Cerrar">×</button></div><div class="modal-body">${content}</div></section>`;
  root.classList.add('open');
  root.querySelectorAll('[data-modal-close]').forEach(button => button.onclick = closeModal);
  onOpen?.(root);
}

function closeModal() {
  const root = document.querySelector('#modal-root');
  if (root) { root.classList.remove('open'); root.innerHTML = ''; }
  state.modalReturnFocus?.focus?.();
  state.modalReturnFocus = null;
}
document.addEventListener('keydown', event => { if (event.key === 'Escape') closeModal(); });
document.addEventListener('click', async event => {
  const button = event.target.closest('[data-refresh-page]');
  if (!button) return;
  setBusy(button, true, 'Actualizando…');
  try {
    await service.loadAssets();
    await renderPage();
    toast('La información se actualizó desde Firebase.');
  } catch (error) {
    toast(error.message || 'No se pudo actualizar la información.', 'error');
    setBusy(button, false);
  }
});

function renderBoot() {
  app.innerHTML = `<main class="boot-screen"><div class="boot-logo">MEV</div><h1>MEV Mantenimiento</h1><p>Preparando el sistema…</p><span class="spinner spinner-light"></span></main>`;
}

function connectionBadge() {
  const connection = service.connectionInfo();
  if (connection.mode === 'demo') return '<span class="connection demo"><span></span> Demostración local</span>';
  if (connection.mode === 'firebase') return `<span class="connection online"><span></span> Firebase ${connection.online ? 'en línea' : 'sin conexión'}</span>`;
  if (connection.firebaseAvailable) return '<span class="connection online"><span></span> Firebase disponible</span>';
  return '<span class="connection offline"><span></span> Modo local disponible</span>';
}

function renderLogin(errorMessage = '') {
  const connection = service.connectionInfo();
  app.innerHTML = `<main class="login-page"><section class="login-intro"><div class="intro-content"><div class="logo-lockup"><div class="logo-mark">MEV</div><span>Mantenimiento</span></div><div class="intro-copy"><p class="eyebrow light">GESTIÓN DE FLOTA MIXTA</p><h1>Mantenimiento y vulcanización en un solo sistema.</h1><p>Registra actividades, controla huellas, revisa trabajos y genera reportes desde cualquier equipo.</p></div><div class="feature-strip"><span>162 activos</span><span>8 perfiles</span><span>Voz y reportes</span></div></div></section><section class="login-panel"><div class="login-card">${connectionBadge()}<div class="login-heading"><h2>Iniciar sesión</h2><p>Usa las mismas credenciales de la aplicación móvil.</p></div>${errorMessage ? `<div class="inline-alert error" role="alert">${escapeHtml(errorMessage)}</div>` : ''}<form id="login-form"><label class="field"><span>Correo electrónico</span><input name="email" id="login-email" type="email" autocomplete="username" placeholder="usuario@empresa.com" required></label><label class="field"><span>Contraseña</span><div class="password-wrap"><input name="password" id="login-password" type="password" autocomplete="current-password" placeholder="••••••••" required><button type="button" id="toggle-password" aria-label="Mostrar contraseña">◉</button></div></label><div class="login-tools"><label class="check-line"><input type="checkbox" id="remember-email"> Recordar correo</label><button type="button" class="text-button" id="reset-password">Recuperar contraseña</button></div><button class="btn btn-primary btn-large full" type="submit" ${connection.firebaseAvailable ? '' : 'disabled'}>Ingresar con Firebase</button></form><div class="divider"><span>o presenta sin depender de Internet</span></div><div class="demo-box"><label class="field compact"><span>Perfil de demostración</span><select id="demo-role">${ROLES.map(role => `<option value="${role}" ${role === 'PLANIFICADOR' ? 'selected' : ''}>${escapeHtml(roleLabel(role))}</option>`).join('')}</select></label><button class="btn btn-secondary full" id="demo-login">Entrar en modo demostración</button></div><p class="login-note">El modo demostración guarda los cambios únicamente en este navegador y no modifica Firebase.</p></div><footer>MEV Mantenimiento Web · Versión ${APP_VERSION}</footer></section></main>`;
  const savedEmail = localStorage.getItem('mev_login_email') || '';
  const emailInput = document.querySelector('#login-email');
  if (savedEmail) { emailInput.value = savedEmail; document.querySelector('#remember-email').checked = true; }
  document.querySelector('#toggle-password').onclick = event => {
    const password = document.querySelector('#login-password');
    password.type = password.type === 'password' ? 'text' : 'password';
    event.currentTarget.setAttribute('aria-label', password.type === 'password' ? 'Mostrar contraseña' : 'Ocultar contraseña');
  };
  document.querySelector('#login-form').onsubmit = async event => {
    event.preventDefault();
    const button = event.submitter;
    const values = new FormData(event.currentTarget);
    const email = String(values.get('email')).trim();
    if (document.querySelector('#remember-email').checked) localStorage.setItem('mev_login_email', email);
    else localStorage.removeItem('mev_login_email');
    setBusy(button, true, 'Ingresando…');
    try { await service.signIn(email, values.get('password')); }
    catch (error) { renderLogin(friendlyAuthError(error)); }
  };
  document.querySelector('#reset-password').onclick = async () => {
    try { await service.resetPassword(emailInput.value); toast('Se envió el enlace de recuperación al correo indicado.'); }
    catch (error) { toast(error.message, 'error'); }
  };
  document.querySelector('#demo-login').onclick = async event => {
    setBusy(event.currentTarget, true, 'Preparando…');
    await service.enterDemo(document.querySelector('#demo-role').value);
  };
}

function friendlyAuthError(error) {
  const code = String(error?.code || '');
  if (/invalid-credential|wrong-password|user-not-found/.test(code)) return 'Correo o contraseña incorrectos.';
  if (/too-many-requests/.test(code)) return 'Se realizaron demasiados intentos. Espera unos minutos.';
  if (/network-request-failed/.test(code)) return 'No hay conexión con Firebase. Puedes usar el modo demostración.';
  return error?.message || 'No se pudo iniciar sesión.';
}

function renderShell() {
  document.querySelectorAll('#toast-region').forEach(region => { if (!app.contains(region)) region.remove(); });
  const menu = ROLE_MENUS[profile().rol] || ['Inicio'];
  if (!menu.includes(state.page)) state.page = 'Inicio';
  app.innerHTML = `<div class="app-shell"><aside class="sidebar" id="sidebar"><div class="sidebar-brand"><div class="sidebar-mark">MEV</div><div><strong>Mantenimiento</strong><small>Gestión de flota</small></div><button class="mobile-close" id="mobile-close" aria-label="Cerrar menú">×</button></div><nav aria-label="Navegación principal">${menu.map(page => `<button class="nav-button ${state.page === page ? 'active' : ''}" data-page="${escapeHtml(page)}"><span aria-hidden="true">${ICONS[page] || '·'}</span><b>${escapeHtml(page)}</b></button>`).join('')}</nav><div class="sidebar-bottom">${connectionBadge()}<div class="user-summary"><div class="avatar">${escapeHtml(initials(fullName()))}</div><div><strong>${escapeHtml(fullName())}</strong><small>${escapeHtml(profile().cargo || roleLabel(profile().rol))}</small></div></div>${service.mode === 'demo' ? '<button class="sidebar-action" id="reset-demo">Restablecer datos de demostración</button>' : ''}<button class="sidebar-action logout" id="logout">Cerrar sesión</button></div></aside><div class="sidebar-overlay" id="sidebar-overlay"></div><main class="main-content"><header class="mobile-header"><button id="mobile-menu" class="icon-button" aria-label="Abrir menú">☰</button><div class="mini-brand"><span>MEV</span> Mantenimiento</div>${connectionBadge()}</header><div id="page" tabindex="-1"></div></main><div id="toast-region" class="toast-region" aria-live="polite"></div></div>`;
  document.querySelectorAll('[data-page]').forEach(button => button.onclick = () => navigate(button.dataset.page));
  document.querySelector('#logout').onclick = () => service.signOut();
  document.querySelector('#mobile-menu').onclick = () => document.body.classList.add('menu-open');
  document.querySelector('#mobile-close').onclick = closeMobileMenu;
  document.querySelector('#sidebar-overlay').onclick = closeMobileMenu;
  document.querySelector('#reset-demo')?.addEventListener('click', () => {
    openModal('Restablecer demostración', '<p>Se eliminarán únicamente los cambios hechos en el modo demostración de este navegador.</p><div class="modal-actions"><button class="btn btn-outline" data-modal-close>Cancelar</button><button class="btn btn-danger" id="confirm-reset-demo">Restablecer datos</button></div>', { onOpen: root => {
      root.querySelector('#confirm-reset-demo').onclick = () => { service.resetDemoData(); closeModal(); toast('Los datos de demostración fueron restablecidos.'); renderPage(); };
    }});
  });
  renderPage();
}

function closeMobileMenu() { document.body.classList.remove('menu-open'); }
function initials(name) { return String(name).split(/\s+/).filter(Boolean).slice(0, 2).map(part => part[0]).join('').toUpperCase() || 'ME'; }
function navigate(page) { state.page = page; state.editing = null; state.matrixPage = 1; closeMobileMenu(); renderShell(); requestAnimationFrame(() => pageRoot()?.focus()); }

async function renderPage() {
  const route = {
    Inicio: pageHome, 'Nuevo mantenimiento': pageMaintenance, Vulcanización: pageVulcanization,
    'Toma general de huella': pageTread, 'Intervención de llanta': pageTire,
    'Mis borradores': pageDrafts, 'Mi historial': pageHistory,
    'Revisión de registros': pageReview, Reportes: pageReports, 'Panel gerencial': pageReports,
    'Matriz base': pageMatrix, Usuarios: pageUsers
  }[state.page] || pageHome;
  try { await route(); }
  catch (error) { console.error(error); pageRoot().innerHTML = `${pageHeader()}<div class="inline-alert error">No se pudo abrir esta sección: ${escapeHtml(error.message)}</div>`; }
}

function quickCard(page, description, accent = '') {
  return `<button class="quick-card ${accent}" data-quick-page="${escapeHtml(page)}"><span class="quick-icon">${ICONS[page] || '·'}</span><span><b>${escapeHtml(page)}</b><small>${escapeHtml(description)}</small></span><i>→</i></button>`;
}

async function pageHome() {
  const root = pageRoot();
  root.innerHTML = `${pageHeader('Inicio', `Bienvenido, ${fullName()}.`)}${loader('Cargando tu actividad…')}`;
  const canReview = REVIEW_ROLES.has(profile().rol) || ['JEFE_OPERACIONES', 'GERENTE_GENERAL'].includes(profile().rol);
  let records = [];
  try { records = await service.listRecords({ ownOnly: !canReview }); }
  catch (error) { root.innerHTML = `${pageHeader('Inicio', `Bienvenido, ${fullName()}.`)}<div class="inline-alert warning">No se pudieron cargar los indicadores: ${escapeHtml(error.message)}</div>${homeQuickAccess()}`; bindQuickLinks(); return; }
  const summary = calculateSummary(records);
  const recent = records.slice(0, 5);
  root.innerHTML = `${pageHeader('Inicio', `Bienvenido, ${fullName()}.`)}<section class="kpi-grid home-kpis">${kpiCard(canReview ? 'Registros totales' : 'Mis registros', summary.total, '▤', 'teal')}${kpiCard('Pendientes de revisión', summary.enviados, '◷', 'amber')}${kpiCard('Aprobados', summary.aprobados, '✓', 'green')}${kpiCard('Huellas críticas', summary.huellasCriticas, '!', summary.huellasCriticas ? 'red' : 'blue')}</section>${homeQuickAccess()}<section class="section-block"><div class="section-heading"><div><h2>Actividad reciente</h2><p>${canReview ? 'Últimos registros del sistema.' : 'Tus últimos registros.'}</p></div></div>${recent.length ? compactRecordList(recent) : emptyState('Sin actividad', 'Todavía no existen registros para mostrar.')}</section>`;
  bindQuickLinks(); bindRecordDetails(recent);
}

function homeQuickAccess() {
  const menu = ROLE_MENUS[profile().rol] || [];
  const descriptions = {
    'Nuevo mantenimiento': 'Registra una actividad preventiva o correctiva.', 'Toma general de huella': 'Mide todas las posiciones del activo.',
    'Intervención de llanta': 'Registra un cambio, reparación o rotación.', 'Mis borradores': 'Continúa trabajos pendientes o devueltos.',
    'Revisión de registros': 'Atiende registros enviados.', Reportes: 'Consulta y exporta indicadores.',
    'Panel gerencial': 'Consulta el resumen ejecutivo.', 'Matriz base': 'Busca activos y configuraciones.'
  };
  const candidates = ['Nuevo mantenimiento', 'Toma general de huella', 'Intervención de llanta', 'Mis borradores', 'Revisión de registros', 'Reportes', 'Panel gerencial', 'Matriz base'].filter(page => menu.includes(page)).slice(0, 4);
  return `<section class="section-block"><div class="section-heading"><div><h2>Accesos rápidos</h2><p>Continúa con una tarea frecuente.</p></div></div><div class="quick-grid">${candidates.map((page, index) => quickCard(page, descriptions[page], index === 0 ? 'featured' : '')).join('')}</div></section>`;
}
function bindQuickLinks() { document.querySelectorAll('[data-quick-page]').forEach(button => button.onclick = () => navigate(button.dataset.quickPage)); }
function kpiCard(label, value, icon, color) { return `<article class="kpi-card ${color}"><div class="kpi-icon">${icon}</div><div><span>${escapeHtml(label)}</span><strong>${Number(value) || 0}</strong></div></article>`; }
function statusBadge(status) { const normalized = normalizeUpper(status || 'SIN ESTADO'); return `<span class="status status-${normalized.toLowerCase()}">${escapeHtml(normalized)}</span>`; }
function recordKey(record) { return `${record._collection}::${record.id}`; }

function compactRecordList(records) {
  return `<div class="record-list">${records.map(record => `<button class="record-row" data-detail-key="${escapeHtml(recordKey(record))}"><span class="record-type-icon">${record._collection === COLLECTIONS.tread ? '◎' : record._collection === COLLECTIONS.tire ? '↻' : '⚙'}</span><span class="record-main"><b>${escapeHtml(record.codigoActivo || 'Sin activo')}</b><small>${escapeHtml(recordLabel(record._collection))} · ${escapeHtml(record.tipoServicio || record.tipoIntervencion || record.estadoGeneral || '')}</small></span><span class="record-reading">${escapeHtml(readingLabel(record))}</span>${statusBadge(record.estadoRegistro)}<span class="record-date">${escapeHtml(formatDate(recordDate(record)))}</span></button>`).join('')}</div>`;
}

function bindRecordDetails(records) {
  const map = new Map(records.map(record => [recordKey(record), record]));
  document.querySelectorAll('[data-detail-key]').forEach(button => button.onclick = () => { const record = map.get(button.dataset.detailKey); if (record) showRecordDetails(record); });
}

function assetOptions({ filter = () => true, selected = '' } = {}) {
  return service.assets.filter(filter).sort((a, b) => String(a.codigo).localeCompare(String(b.codigo))).map(asset => `<option value="${escapeHtml(asset.codigo)}" ${asset.codigo === selected ? 'selected' : ''}>${escapeHtml(asset.codigo)} — ${escapeHtml(asset.subtipo || asset.tipo || 'SIN TIPO')}</option>`).join('');
}
function selectedAsset(code) { return service.assets.find(asset => normalizeUpper(asset.codigo) === normalizeUpper(code)); }
function assetSummary(asset) {
  if (!asset) return '<span>Selecciona un activo para visualizar su configuración.</span>';
  const reading = asset.usaKilometraje ? `${formatNumber(asset.kilometraje)} km` : asset.usaHorometro ? `${formatNumber(asset.horometro)} h` : 'Sin lectura base';
  const positions = assetPositionCount(asset);
  return `<div class="asset-summary-main"><b>${escapeHtml(asset.codigo)}</b><span>${escapeHtml([asset.marca, asset.modelo].filter(Boolean).join(' ') || asset.subtipo || asset.tipo)}</span></div><div class="asset-summary-data"><span><small>Ubicación</small>${escapeHtml(asset.ubicacionActual || 'Sin registro')}</span><span><small>Indicador</small>${escapeHtml(asset.indicador || 'N/A')}</span><span><small>Última lectura</small>${escapeHtml(reading)}</span><span><small>Posiciones de llanta</small>${positions || 'No aplica'}</span><span><small>Estado</small>${escapeHtml(asset.status || 'Sin registro')}</span></div>`;
}
function editingBanner(record) { return record ? `<div class="editing-banner"><div><b>${record.estadoRegistro === 'DEVUELTO' ? 'Corrigiendo registro devuelto' : 'Editando borrador'}</b><span>${record.estadoRegistro === 'DEVUELTO' ? escapeHtml(record.motivoDevolucion || 'El revisor no indicó un motivo.') : 'Los cambios se guardarán en el mismo registro.'}</span></div><button class="btn btn-outline btn-small" id="cancel-edit">Cancelar edición</button></div>` : ''; }
function formActions(prefix = '') { return `<div class="form-actions"><button class="btn btn-outline" type="button" data-save-status="BORRADOR">Guardar borrador</button><button class="btn btn-primary" type="button" data-save-status="ENVIADO">${prefix || 'Enviar a revisión'}</button></div>`; }
function voicePanel(type) { const supported = speechSupported(); return `<div class="voice-panel"><div><span class="voice-symbol">🎙</span><div><b>Ingreso asistido por voz</b><small>${escapeHtml(voiceHelp(type))}</small></div></div><button type="button" class="btn btn-voice" id="voice-start" ${supported ? '' : 'disabled'}>${supported ? 'Dictar datos' : 'Voz no disponible'}</button><div class="voice-transcript" id="voice-transcript" hidden></div></div>`; }

function evidenceField(record = null) {
  const current = record?.fotoUrl || record?.fotoDataUrl;
  return `<div class="field field-full"><span>Evidencia fotográfica <em>Opcional</em></span><label class="file-drop"><input type="file" name="evidence" id="evidence" accept="image/*" capture="environment"><span class="file-icon">▧</span><span><b>Seleccionar o tomar una fotografía</b><small>JPG o PNG, máximo recomendado 5 MB.</small></span></label><div id="evidence-preview" class="evidence-preview" ${current ? '' : 'hidden'}>${current ? `<img src="${escapeHtml(current)}" alt="Evidencia actual"><span>Evidencia guardada</span>` : ''}</div></div>`;
}

function bindEvidencePreview() {
  const input = document.querySelector('#evidence'); const preview = document.querySelector('#evidence-preview');
  if (!input || !preview) return;
  input.onchange = () => {
    const file = input.files?.[0]; if (!file) return;
    if (!file.type.startsWith('image/')) { toast('La evidencia debe ser una imagen.', 'error'); input.value = ''; return; }
    if (file.size > 8 * 1024 * 1024) { toast('La imagen supera 8 MB. Selecciona una fotografía más liviana.', 'error'); input.value = ''; return; }
    const url = URL.createObjectURL(file); preview.hidden = false;
    preview.innerHTML = `<img src="${url}" alt="Vista previa de evidencia"><span>${escapeHtml(file.name)} · ${(file.size / 1024 / 1024).toFixed(2)} MB</span>`;
  };
}

function bindAssetSelector(selectId, infoId, afterChange = null) {
  const select = document.querySelector(`#${selectId}`); const info = document.querySelector(`#${infoId}`); if (!select || !info) return;
  const update = () => { const asset = selectedAsset(select.value); info.innerHTML = assetSummary(asset); info.classList.toggle('has-asset', Boolean(asset)); afterChange?.(asset); };
  select.onchange = update; update();
}
function bindCancelEdit() { document.querySelector('#cancel-edit')?.addEventListener('click', () => { state.editing = null; renderPage(); }); }

function bindVoice(form, type, afterApply = null) {
  const button = document.querySelector('#voice-start'); const transcript = document.querySelector('#voice-transcript'); if (!button || !speechSupported()) return;
  let controller = null; let listening = false;
  button.onclick = () => {
    if (listening) {
      button.disabled = true; button.textContent = 'Procesando…'; controller?.stop(); return;
    }
    listening = true; button.classList.add('listening'); button.setAttribute('aria-pressed', 'true'); button.textContent = 'Finalizar dictado'; transcript.hidden = false; transcript.textContent = 'Habla ahora. Puedes dictar varios campos seguidos…';
    controller = listenForCommand(type, {
      onTranscript: text => { transcript.textContent = text || 'Escuchando…'; },
      onResult: result => {
        applyVoiceFields(form, result.fields); afterApply?.(result);
        const detected = Object.keys(result.fields).length + Object.keys(result.positions || {}).length;
        transcript.innerHTML = `<b>Texto reconocido:</b> ${escapeHtml(result.transcript)}<br><b>Campos detectados:</b> ${detected}`;
        toast(detected ? `${detected} campo${detected === 1 ? '' : 's'} reconocido${detected === 1 ? '' : 's'} y aplicado${detected === 1 ? '' : 's'}. Revisa antes de guardar.` : 'Se reconoció el texto, pero no se identificaron campos. Usa los nombres mostrados en el ejemplo.', detected ? 'success' : 'warning');
      },
      onError: message => { transcript.textContent = message; toast(message, 'error'); },
      onEnd: () => { listening = false; controller = null; button.disabled = false; button.classList.remove('listening'); button.setAttribute('aria-pressed', 'false'); button.textContent = 'Dictar datos'; }
    });
  };
}

function applyVoiceFields(form, fields) {
  Object.entries(fields).forEach(([name, value]) => {
    const input = form.elements.namedItem(name); if (!input) return;
    if (name === 'codigoActivo') {
      const exact = service.assets.find(asset => normalizeUpper(asset.codigo) === normalizeUpper(value));
      const numeric = String(value).match(/(\d+)$/)?.[1];
      const approximate = exact || service.assets.find(asset => numeric && String(asset.codigo).endsWith(numeric.padStart(4, '0')));
      if (approximate) input.value = approximate.codigo;
    } else input.value = value;
    input.dispatchEvent(new Event('change', { bubbles: true })); input.dispatchEvent(new Event('input', { bubbles: true }));
  });
}

async function pageMaintenance() {
  const root = pageRoot();
  const record = state.editing?._collection === COLLECTIONS.maintenance ? state.editing : null;
  root.innerHTML = `${pageHeader()}${editingBanner(record)}${voicePanel('maintenance')}<section class="form-card"><form id="maintenance-form" novalidate>
    <div class="form-section"><div class="form-section-title"><span>1</span><div><h2>Activo y servicio</h2><p>Selecciona la unidad e ingresa su lectura actual.</p></div></div><div class="form-grid">
      <label class="field field-full"><span>Activo <b>*</b></span><select name="codigoActivo" id="maintenance-asset" required><option value="">Seleccionar activo…</option>${assetOptions({ selected: record?.codigoActivo || '' })}</select></label><div id="maintenance-asset-info" class="asset-summary field-full"></div>
      <label class="field"><span>Tipo de servicio <b>*</b></span><select name="tipoServicio"><option value="PREVENTIVO" ${record?.tipoServicio === 'PREVENTIVO' ? 'selected' : ''}>Preventivo</option><option value="CORRECTIVO" ${record?.tipoServicio === 'CORRECTIVO' ? 'selected' : ''}>Correctivo</option></select></label>
      <label class="field"><span>Orden de trabajo</span><input name="ordenTrabajo" value="${escapeHtml(record?.ordenTrabajo || '')}" placeholder="Ej. OT-2026-001"></label>
      <label class="field" id="km-field"><span>Kilometraje</span><input name="kilometraje" type="number" min="0" step="0.01" value="${escapeHtml(record?.kilometraje ?? '')}" placeholder="0"></label>
      <label class="field" id="hour-field"><span>Horómetro</span><input name="horometro" type="number" min="0" step="0.01" value="${escapeHtml(record?.horometro ?? '')}" placeholder="0"></label>
    </div></div>
    <div class="form-section"><div class="form-section-title"><span>2</span><div><h2>Trabajo ejecutado</h2><p>Describe de manera concreta la actividad realizada.</p></div></div><div class="form-grid">
      <label class="field field-full"><span>Acción ejecutada <b>* al enviar</b></span><textarea name="accionEjecutada" rows="4" placeholder="Detalle del diagnóstico, reparación o mantenimiento…">${escapeHtml(record?.accionEjecutada || '')}</textarea></label>
      <label class="field field-full"><span>Observaciones</span><textarea name="observaciones" rows="3" placeholder="Novedades, repuestos pendientes o recomendaciones…">${escapeHtml(record?.observaciones || '')}</textarea></label>
      <label class="field"><span>Número de pedido</span><input name="numeroPedido" value="${escapeHtml(record?.numeroPedido || '')}" placeholder="Ej. PED-001"></label>${evidenceField(record)}
    </div></div>${formActions(record ? 'Guardar y enviar' : '')}</form></section>`;
  bindCancelEdit(); bindEvidencePreview();
  bindAssetSelector('maintenance-asset', 'maintenance-asset-info', asset => {
    const selectedType = document.querySelector('[name="tipoServicio"]')?.value;
    if (asset && selectedType === 'PREVENTIVO' && !assetAllows(asset, 'permitePreventivo')) toast('Este activo no está configurado para mantenimiento preventivo.', 'warning');
    document.querySelector('#km-field')?.classList.toggle('suggested', Boolean(asset?.usaKilometraje));
    document.querySelector('#hour-field')?.classList.toggle('suggested', Boolean(asset?.usaHorometro));
  });
  const form = document.querySelector('#maintenance-form'); bindVoice(form, 'maintenance');
  document.querySelectorAll('[data-save-status]').forEach(button => button.onclick = () => saveMaintenance(form, button.dataset.saveStatus, button, record));
}

async function saveMaintenance(form, status, button, record) {
  const values = new FormData(form); const code = normalizeUpper(values.get('codigoActivo')); const asset = selectedAsset(code);
  if (!asset) return toast('Debes seleccionar un activo válido.', 'error');
  const type = normalizeUpper(values.get('tipoServicio'));
  if (type === 'PREVENTIVO' && !assetAllows(asset, 'permitePreventivo')) return toast('El activo no permite mantenimiento preventivo.', 'error');
  if (type === 'CORRECTIVO' && !assetAllows(asset, 'permiteCorrectivo')) return toast('El activo no permite mantenimiento correctivo.', 'error');
  const action = String(values.get('accionEjecutada') || '').trim();
  if (status === 'ENVIADO' && !action) return toast('Debes ingresar la acción ejecutada antes de enviar.', 'error');
  const km = values.get('kilometraje'); const hours = values.get('horometro');
  if (status === 'ENVIADO' && asset.usaKilometraje && km === '') return toast('Ingresa el kilometraje actual del activo.', 'error');
  if (status === 'ENVIADO' && asset.usaHorometro && hours === '') return toast('Ingresa el horómetro actual del activo.', 'error');
  const data = {
    codigoActivo: code, tipoServicio: type, kilometraje: km === '' ? null : Number(km), horometro: hours === '' ? null : Number(hours),
    accionEjecutada: action, observaciones: String(values.get('observaciones') || '').trim(), ordenTrabajo: normalizeUpper(values.get('ordenTrabajo')),
    numeroPedido: normalizeUpper(values.get('numeroPedido')), estadoRegistro: status
  };
  setBusy(button, true, status === 'BORRADOR' ? 'Guardando…' : 'Enviando…');
  try {
    const evidence = await optimizeImage(values.get('evidence'));
    const result = await service.saveRecord(COLLECTIONS.maintenance, data, { id: record?.id, evidenceFile: evidence });
    state.editing = null; toast(status === 'BORRADOR' ? 'El borrador fue guardado.' : 'El mantenimiento fue enviado a revisión.');
    if (result.warning) toast(result.warning, 'warning', 7000);
    state.page = status === 'BORRADOR' ? 'Mis borradores' : 'Mi historial'; renderShell();
  } catch (error) { toast(`No se pudo guardar: ${error.message}`, 'error'); setBusy(button, false); }
}

function pageVulcanization() {
  pageRoot().innerHTML = `${pageHeader()}<div class="module-grid"><article class="module-card"><div class="module-visual tread-visual"><span>P1</span><span>P2</span><span>P3</span><span>P4</span></div><div><p class="eyebrow">CONTROL SEMANAL</p><h2>Toma general de huella</h2><p>Registra hasta 12 posiciones según la configuración del activo e identifica mediciones críticas.</p><button class="btn btn-primary" data-quick-page="Toma general de huella">Nueva toma de huella</button></div></article><article class="module-card"><div class="module-visual tire-visual">↻</div><div><p class="eyebrow">INTERVENCIÓN</p><h2>Intervención de llanta</h2><p>Documenta cambios, rotaciones, reparaciones, montajes, desmontajes y bajas.</p><button class="btn btn-primary" data-quick-page="Intervención de llanta">Nueva intervención</button></div></article></div>`;
  bindQuickLinks();
}

async function pageTread() {
  const root = pageRoot(); const record = state.editing?._collection === COLLECTIONS.tread ? state.editing : null;
  root.innerHTML = `${pageHeader()}${editingBanner(record)}${voicePanel('tread')}<section class="form-card"><form id="tread-form" novalidate>
    <div class="form-section"><div class="form-section-title"><span>1</span><div><h2>Activo y ubicación</h2><p>Solo se muestran activos configurados para toma de huella.</p></div></div><div class="form-grid">
      <label class="field field-full"><span>Activo <b>*</b></span><select name="codigoActivo" id="tread-asset"><option value="">Seleccionar activo…</option>${assetOptions({ filter: asset => assetAllows(asset, 'permiteTomaHuella'), selected: record?.codigoActivo || '' })}</select></label><div id="tread-asset-info" class="asset-summary field-full"></div>
      <label class="field"><span>Proyecto / campamento <b>* al enviar</b></span><input name="proyecto" value="${escapeHtml(record?.proyecto || '')}" placeholder="Ej. Loja Vía Antigua"></label>
      <label class="field"><span>Nombre del técnico <b>* al enviar</b></span><input name="nombreTecnico" value="${escapeHtml(record?.nombreTecnico || fullName())}" placeholder="Nombre completo"></label>
      <label class="field"><span>Kilometraje</span><input name="kilometraje" type="number" min="0" step="0.01" value="${escapeHtml(record?.kilometraje ?? '')}"></label>
      <label class="field"><span>Horómetro</span><input name="horometro" type="number" min="0" step="0.01" value="${escapeHtml(record?.horometro ?? '')}"></label>
    </div></div>
    <div class="form-section"><div class="form-section-title"><span>2</span><div><h2>Mediciones por posición</h2><p>Las huellas iguales o menores a 6 mm se marcarán como críticas.</p></div></div><div id="tread-positions" class="position-grid">${emptyState('Selecciona un activo', 'Las posiciones se habilitarán automáticamente.')}</div></div>
    <div class="form-section"><div class="form-section-title"><span>3</span><div><h2>Resultado de inspección</h2><p>Resume el estado general y las novedades encontradas.</p></div></div><div class="form-grid">
      <label class="field"><span>Estado general</span><select name="estadoGeneral"><option value="BUENO" ${record?.estadoGeneral === 'BUENO' ? 'selected' : ''}>Bueno</option><option value="REGULAR" ${record?.estadoGeneral === 'REGULAR' ? 'selected' : ''}>Regular</option><option value="MALO" ${record?.estadoGeneral === 'MALO' ? 'selected' : ''}>Malo</option></select></label>
      <label class="field field-full"><span>Novedad</span><textarea name="novedad" rows="3" placeholder="Desgaste irregular, presión, daño lateral…">${escapeHtml(record?.novedad || '')}</textarea></label>${evidenceField(record)}
    </div></div>${formActions(record ? 'Guardar y enviar' : '')}</form></section>`;
  bindCancelEdit(); bindEvidencePreview(); const form = document.querySelector('#tread-form'); const savedValues = record ? getTreadValues(record) : [];
  const drawPositions = (asset, values = savedValues) => {
    const container = document.querySelector('#tread-positions'); const count = assetPositionCount(asset);
    if (!count) { container.innerHTML = emptyState('Sin posiciones configuradas', 'Selecciona otro activo o corrige la matriz base.'); return; }
    container.innerHTML = Array.from({ length: count }, (_, index) => `<label class="position-field"><span>P${index + 1}</span><input name="P${index + 1}" type="number" min="0" max="30" step="0.1" value="${escapeHtml(values[index] ?? '')}" inputmode="decimal"><small>milímetros</small></label>`).join('');
    container.querySelectorAll('input').forEach(input => { input.oninput = () => input.closest('.position-field').classList.toggle('critical', Number(input.value) > 0 && Number(input.value) <= 6); input.dispatchEvent(new Event('input')); });
  };
  bindAssetSelector('tread-asset', 'tread-asset-info', asset => drawPositions(asset, asset?.codigo === record?.codigoActivo ? savedValues : []));
  bindVoice(form, 'tread', result => { const asset = selectedAsset(form.elements.codigoActivo.value); drawPositions(asset, []); Object.entries(result.positions).forEach(([name, value]) => { const input = form.elements.namedItem(name); if (input) { input.value = value; input.dispatchEvent(new Event('input')); } }); });
  document.querySelectorAll('[data-save-status]').forEach(button => button.onclick = () => saveTread(form, button.dataset.saveStatus, button, record));
}

async function saveTread(form, status, button, record) {
  const values = new FormData(form); const code = normalizeUpper(values.get('codigoActivo')); const asset = selectedAsset(code);
  if (!asset || !assetAllows(asset, 'permiteTomaHuella')) return toast('Selecciona un activo habilitado para toma de huella.', 'error');
  const count = assetPositionCount(asset); if (!count) return toast('El activo no tiene posiciones configuradas.', 'error');
  const treads = Array.from({ length: count }, (_, index) => { const value = values.get(`P${index + 1}`); return value === '' ? null : Number(value); });
  if (status === 'ENVIADO' && treads.some(value => value === null || value < 0)) return toast('Completa todas las mediciones antes de enviar.', 'error');
  if (treads.some(value => value !== null && (!Number.isFinite(value) || value > 30))) return toast('Revisa las huellas: deben estar entre 0 y 30 mm.', 'error');
  const project = String(values.get('proyecto') || '').trim(); const technician = String(values.get('nombreTecnico') || '').trim();
  if (status === 'ENVIADO' && (!project || !technician)) return toast('Completa el proyecto y el nombre del técnico antes de enviar.', 'error');
  const data = { codigoActivo: code, proyecto: project.toUpperCase(), kilometraje: values.get('kilometraje') === '' ? null : Number(values.get('kilometraje')), horometro: values.get('horometro') === '' ? null : Number(values.get('horometro')), huellas: treads, estadoGeneral: normalizeUpper(values.get('estadoGeneral')), novedad: String(values.get('novedad') || '').trim(), nombreTecnico: technician, estadoRegistro: status };
  Array.from({ length: 12 }, (_, index) => { data[`P${index + 1}`] = treads[index] ?? null; });
  setBusy(button, true, status === 'BORRADOR' ? 'Guardando…' : 'Enviando…');
  try {
    const evidence = await optimizeImage(values.get('evidence')); const result = await service.saveRecord(COLLECTIONS.tread, data, { id: record?.id, evidenceFile: evidence });
    state.editing = null; toast(status === 'BORRADOR' ? 'La toma de huella se guardó como borrador.' : 'La toma de huella fue enviada a revisión.'); if (result.warning) toast(result.warning, 'warning', 7000);
    state.page = status === 'BORRADOR' ? 'Mis borradores' : 'Mi historial'; renderShell();
  } catch (error) { toast(`No se pudo guardar: ${error.message}`, 'error'); setBusy(button, false); }
}

async function pageTire() {
  const root = pageRoot(); const record = state.editing?._collection === COLLECTIONS.tire ? state.editing : null;
  root.innerHTML = `${pageHeader()}${editingBanner(record)}${voicePanel('tire')}<section class="form-card"><form id="tire-form" novalidate>
    <div class="form-section"><div class="form-section-title"><span>1</span><div><h2>Activo e intervención</h2><p>Identifica la unidad, la ubicación y el tipo de trabajo.</p></div></div><div class="form-grid">
      <label class="field field-full"><span>Activo <b>*</b></span><select name="codigoActivo" id="tire-asset"><option value="">Seleccionar activo…</option>${assetOptions({ filter: asset => assetAllows(asset, 'permiteIntervencionLlanta'), selected: record?.codigoActivo || '' })}</select></label><div id="tire-asset-info" class="asset-summary field-full"></div>
      <label class="field"><span>Proyecto / campamento <b>* al enviar</b></span><input name="proyecto" value="${escapeHtml(record?.proyecto || '')}"></label>
      <label class="field"><span>Tipo de intervención <b>*</b></span><select name="tipoIntervencion">${['CAMBIO', 'ROTACION', 'REPARACION', 'MONTAJE', 'DESMONTAJE', 'BAJA'].map(type => `<option value="${type}" ${record?.tipoIntervencion === type ? 'selected' : ''}>${escapeHtml(type[0] + type.slice(1).toLowerCase())}</option>`).join('')}</select></label>
      <label class="field"><span>Kilometraje</span><input name="kilometraje" type="number" min="0" step="0.01" value="${escapeHtml(record?.kilometraje ?? '')}"></label><label class="field"><span>Horómetro</span><input name="horometro" type="number" min="0" step="0.01" value="${escapeHtml(record?.horometro ?? '')}"></label>
      <label class="field"><span>Posición <b>* al enviar</b></span><select name="posicion" id="tire-position"><option value="">Selecciona primero el activo…</option></select></label><label class="field"><span>Huella actual (mm)</span><input name="huella" type="number" min="0" max="30" step="0.1" value="${escapeHtml(record?.huella ?? '')}"></label>
    </div></div>
    <div class="form-section"><div class="form-section-title"><span>2</span><div><h2>Identificación de la llanta</h2><p>Registra los datos visibles y el motivo de la intervención.</p></div></div><div class="form-grid">
      <label class="field"><span>Marca de llanta</span><input name="marcaLlanta" value="${escapeHtml(record?.marcaLlanta || '')}" placeholder="Ej. Bridgestone"></label><label class="field"><span>Medida</span><input name="medidaLlanta" value="${escapeHtml(record?.medidaLlanta || '')}" placeholder="Ej. 12R22.5"></label>
      <label class="field"><span>Serie / identificación</span><input name="serieLlanta" value="${escapeHtml(record?.serieLlanta || '')}"></label><label class="field"><span>Nombre del técnico <b>* al enviar</b></span><input name="nombreTecnico" value="${escapeHtml(record?.nombreTecnico || fullName())}"></label>
      <label class="field field-full"><span>Motivo de intervención <b>* al enviar</b></span><textarea name="motivo" rows="3" placeholder="Desgaste, pinchazo, corte, daño lateral…">${escapeHtml(record?.motivo || '')}</textarea></label><label class="field field-full"><span>Resumen / observaciones</span><textarea name="observaciones" rows="3" placeholder="Actividad ejecutada y resultado…">${escapeHtml(record?.observaciones || '')}</textarea></label>${evidenceField(record)}
    </div></div>${formActions(record ? 'Guardar y enviar' : '')}</form></section>`;
  bindCancelEdit(); bindEvidencePreview(); const form = document.querySelector('#tire-form');
  const drawPositions = asset => { const select = document.querySelector('#tire-position'); const current = select.value || record?.posicion || ''; const count = assetPositionCount(asset); select.innerHTML = `<option value="">Seleccionar posición…</option>${Array.from({ length: count }, (_, index) => `<option value="P${index + 1}" ${current === `P${index + 1}` ? 'selected' : ''}>P${index + 1}</option>`).join('')}`; };
  bindAssetSelector('tire-asset', 'tire-asset-info', drawPositions); bindVoice(form, 'tire', () => drawPositions(selectedAsset(form.elements.codigoActivo.value)));
  document.querySelectorAll('[data-save-status]').forEach(button => button.onclick = () => saveTire(form, button.dataset.saveStatus, button, record));
}

async function saveTire(form, status, button, record) {
  const values = new FormData(form); const code = normalizeUpper(values.get('codigoActivo')); const asset = selectedAsset(code);
  if (!asset || !assetAllows(asset, 'permiteIntervencionLlanta')) return toast('Selecciona un activo habilitado para intervención de llantas.', 'error');
  const position = normalizeUpper(values.get('posicion')); const project = String(values.get('proyecto') || '').trim(); const technician = String(values.get('nombreTecnico') || '').trim(); const reason = String(values.get('motivo') || '').trim();
  if (status === 'ENVIADO' && (!project || !position || !technician || !reason)) return toast('Completa proyecto, posición, técnico y motivo antes de enviar.', 'error');
  if (position && !validatePosition(position, asset)) return toast('La posición no corresponde a la configuración del activo.', 'error');
  const tread = values.get('huella') === '' ? null : Number(values.get('huella')); if (tread !== null && (!Number.isFinite(tread) || tread < 0 || tread > 30)) return toast('La huella debe estar entre 0 y 30 mm.', 'error');
  const data = { codigoActivo: code, proyecto: project.toUpperCase(), kilometraje: values.get('kilometraje') === '' ? null : Number(values.get('kilometraje')), horometro: values.get('horometro') === '' ? null : Number(values.get('horometro')), tipoIntervencion: normalizeUpper(values.get('tipoIntervencion')), posicion, huella: tread, marcaLlanta: normalizeUpper(values.get('marcaLlanta')), medidaLlanta: normalizeUpper(values.get('medidaLlanta')), serieLlanta: normalizeUpper(values.get('serieLlanta')), motivo: reason, observaciones: String(values.get('observaciones') || '').trim(), nombreTecnico: technician, estadoRegistro: status };
  setBusy(button, true, status === 'BORRADOR' ? 'Guardando…' : 'Enviando…');
  try {
    const evidence = await optimizeImage(values.get('evidence')); const result = await service.saveRecord(COLLECTIONS.tire, data, { id: record?.id, evidenceFile: evidence });
    state.editing = null; toast(status === 'BORRADOR' ? 'La intervención se guardó como borrador.' : 'La intervención fue enviada a revisión.'); if (result.warning) toast(result.warning, 'warning', 7000);
    state.page = status === 'BORRADOR' ? 'Mis borradores' : 'Mi historial'; renderShell();
  } catch (error) { toast(`No se pudo guardar: ${error.message}`, 'error'); setBusy(button, false); }
}

async function optimizeImage(file) {
  if (!(file instanceof File) || !file.size) return null;
  if (!file.type.startsWith('image/') || file.size < 800 * 1024) return file;
  try {
    const bitmap = await createImageBitmap(file); const scale = Math.min(1, 1600 / Math.max(bitmap.width, bitmap.height)); const canvas = document.createElement('canvas');
    canvas.width = Math.round(bitmap.width * scale); canvas.height = Math.round(bitmap.height * scale); canvas.getContext('2d').drawImage(bitmap, 0, 0, canvas.width, canvas.height);
    const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/jpeg', 0.78)); bitmap.close(); return new File([blob], file.name.replace(/\.[^.]+$/, '.jpg'), { type: 'image/jpeg' });
  } catch { return file; }
}

async function pageDrafts() {
  const root = pageRoot(); root.innerHTML = `${pageHeader()}${loader('Buscando borradores y devoluciones…')}`;
  const records = await service.listRecords({ ownOnly: true, statuses: ['BORRADOR', 'DEVUELTO'] }); state.records = records;
  root.innerHTML = `${pageHeader()}<div class="toolbar"><label class="search-box"><span>⌕</span><input id="draft-search" placeholder="Buscar por activo, tipo o contenido…"></label><div class="toolbar-summary"><b>${records.length}</b> pendientes</div></div><div id="draft-results">${records.length ? recordsTable(records, 'draft') : emptyState('Todo al día', 'No tienes borradores ni registros devueltos pendientes.')}</div>`;
  bindTableActions(records, 'draft');
  document.querySelector('#draft-search')?.addEventListener('input', event => {
    const query = normalizeText(event.target.value); const filtered = records.filter(record => normalizeText(JSON.stringify(record)).includes(query));
    document.querySelector('#draft-results').innerHTML = filtered.length ? recordsTable(filtered, 'draft') : emptyState('Sin coincidencias', 'Prueba con otro término de búsqueda.'); bindTableActions(filtered, 'draft');
  });
}

async function pageHistory() {
  const root = pageRoot(); root.innerHTML = `${pageHeader()}${loader('Cargando tu historial…')}`;
  const records = await service.listRecords({ ownOnly: true, statuses: ['ENVIADO', 'APROBADO', 'DEVUELTO'] }); state.records = records;
  root.innerHTML = `${pageHeader()}${recordFilters('history', records)}<div id="history-results">${records.length ? recordsTable(records, 'history') : emptyState('Sin historial', 'Los registros aparecerán aquí después de enviarlos.')}</div>`;
  bindGenericRecordFilters('history', records, filtered => { document.querySelector('#history-results').innerHTML = filtered.length ? recordsTable(filtered, 'history') : emptyState('Sin resultados', 'No existen registros con los filtros seleccionados.'); bindTableActions(filtered, 'history'); });
  bindTableActions(records, 'history');
}

function recordFilters(prefix, records) {
  return `<div class="filter-bar"><label class="search-box"><span>⌕</span><input id="${prefix}-search" placeholder="Buscar activo o contenido…"></label><label><span>Tipo</span><select id="${prefix}-type"><option value="">Todos</option>${Object.entries(RECORD_LABELS).map(([value, label]) => `<option value="${value}">${escapeHtml(label)}</option>`).join('')}</select></label><label><span>Estado</span><select id="${prefix}-status"><option value="">Todos</option><option>ENVIADO</option><option>APROBADO</option><option>DEVUELTO</option><option>BORRADOR</option></select></label><div class="toolbar-summary"><b>${records.length}</b> registros</div></div>`;
}

function bindGenericRecordFilters(prefix, records, draw) {
  const apply = () => {
    const query = normalizeText(document.querySelector(`#${prefix}-search`).value); const type = document.querySelector(`#${prefix}-type`).value; const status = document.querySelector(`#${prefix}-status`).value;
    draw(records.filter(record => (!query || normalizeText(JSON.stringify(record)).includes(query)) && (!type || record._collection === type) && (!status || normalizeUpper(record.estadoRegistro) === status)));
  };
  [`#${prefix}-search`, `#${prefix}-type`, `#${prefix}-status`].forEach(selector => document.querySelector(selector)?.addEventListener('input', apply));
}

function recordsTable(records, mode = 'history') {
  return `<div class="table-card"><div class="table-scroll"><table><thead><tr><th>Tipo</th><th>Activo</th><th>Detalle</th><th>Lectura</th><th>Estado</th><th>Fecha</th><th class="actions-column">Acciones</th></tr></thead><tbody>${records.map(record => `<tr><td><span class="type-label">${escapeHtml(recordLabel(record._collection))}</span></td><td><b>${escapeHtml(record.codigoActivo || '—')}</b><small>${escapeHtml(record.proyecto || '')}</small></td><td>${escapeHtml(record.tipoServicio || record.tipoIntervencion || record.estadoGeneral || '—')}<small>${escapeHtml(record.accionEjecutada || record.motivo || record.novedad || '')}</small></td><td>${escapeHtml(readingLabel(record))}</td><td>${statusBadge(record.estadoRegistro)}${record.estadoRegistro === 'DEVUELTO' ? `<small class="return-reason">${escapeHtml(record.motivoDevolucion || 'Sin motivo')}</small>` : ''}</td><td>${escapeHtml(formatDate(recordDate(record)))}</td><td class="table-actions"><button class="btn-icon" data-detail-key="${escapeHtml(recordKey(record))}" title="Ver detalle">Ver</button>${mode === 'draft' ? `<button class="btn btn-primary btn-small" data-edit-key="${escapeHtml(recordKey(record))}">${record.estadoRegistro === 'DEVUELTO' ? 'Corregir' : 'Editar'}</button>` : ''}${mode === 'review' ? `<button class="btn btn-success btn-small" data-approve-key="${escapeHtml(recordKey(record))}">Aprobar</button><button class="btn btn-outline btn-small" data-return-key="${escapeHtml(recordKey(record))}">Devolver</button>` : ''}</td></tr>`).join('')}</tbody></table></div></div>`;
}

function bindTableActions(records, mode) {
  const map = new Map(records.map(record => [recordKey(record), record])); bindRecordDetails(records);
  if (mode === 'draft') document.querySelectorAll('[data-edit-key]').forEach(button => button.onclick = () => editRecord(map.get(button.dataset.editKey)));
  if (mode === 'review') {
    document.querySelectorAll('[data-approve-key]').forEach(button => button.onclick = () => reviewDecision(map.get(button.dataset.approveKey), 'APROBADO'));
    document.querySelectorAll('[data-return-key]').forEach(button => button.onclick = () => reviewDecision(map.get(button.dataset.returnKey), 'DEVUELTO'));
  }
}

function editRecord(record) {
  if (!record) return; state.editing = record;
  state.page = record._collection === COLLECTIONS.maintenance ? 'Nuevo mantenimiento' : record._collection === COLLECTIONS.tread ? 'Toma general de huella' : 'Intervención de llanta'; renderShell();
}

function showRecordDetails(record) {
  const common = [['Tipo de registro', recordLabel(record._collection)], ['Activo', record.codigoActivo], ['Estado', record.estadoRegistro], ['Lectura', readingLabel(record)], ['Proyecto', record.proyecto], ['Técnico / usuario', record.nombreTecnico || record.emailUsuario], ['Creado', formatDate(record.fechaCreacion)], ['Última actualización', formatDate(record.fechaActualizacion)]];
  const specific = record._collection === COLLECTIONS.maintenance
    ? [['Servicio', record.tipoServicio], ['Acción ejecutada', record.accionEjecutada], ['Observaciones', record.observaciones], ['Orden de trabajo', record.ordenTrabajo], ['Número de pedido', record.numeroPedido]]
    : record._collection === COLLECTIONS.tread
      ? [['Estado general', record.estadoGeneral], ['Novedad', record.novedad], ['Mediciones', getTreadValues(record).map((value, index) => value === null ? '' : `P${index + 1}: ${value} mm`).filter(Boolean).join(' · ')]]
      : [['Intervención', record.tipoIntervencion], ['Posición', record.posicion], ['Huella', record.huella === null || record.huella === undefined ? '' : `${record.huella} mm`], ['Marca', record.marcaLlanta], ['Medida', record.medidaLlanta], ['Serie', record.serieLlanta], ['Motivo', record.motivo], ['Observaciones', record.observaciones]];
  const photo = record.fotoUrl || record.fotoDataUrl;
  openModal(`${recordLabel(record._collection)} · ${record.codigoActivo || ''}`, `<div class="detail-grid">${[...common, ...specific].filter(([, value]) => value !== '' && value !== null && value !== undefined).map(([label, value]) => `<div class="detail-item ${String(value).length > 80 ? 'wide' : ''}"><span>${escapeHtml(label)}</span><b>${escapeHtml(value)}</b></div>`).join('')}</div>${record.motivoDevolucion ? `<div class="inline-alert error"><b>Motivo de devolución:</b> ${escapeHtml(record.motivoDevolucion)}</div>` : ''}${photo ? `<div class="detail-photo"><span>Evidencia fotográfica</span><img src="${escapeHtml(photo)}" alt="Evidencia del registro"></div>` : ''}<div class="modal-actions"><button class="btn btn-primary" data-modal-close>Cerrar</button></div>`, { wide: true });
}

async function pageReview() {
  const root = pageRoot(); root.innerHTML = `${pageHeader()}${loader('Cargando registros pendientes…')}`;
  const records = await service.listRecords({ statuses: ['ENVIADO'] }); state.records = records;
  root.innerHTML = `${pageHeader()}${recordFilters('review', records)}<div id="review-results">${records.length ? recordsTable(records, 'review') : emptyState('Sin registros pendientes', 'Todos los registros enviados ya fueron atendidos.')}</div>`;
  bindGenericRecordFilters('review', records, filtered => { document.querySelector('#review-results').innerHTML = filtered.length ? recordsTable(filtered, 'review') : emptyState('Sin resultados', 'No hay registros pendientes con esos filtros.'); bindTableActions(filtered, 'review'); });
  bindTableActions(records, 'review');
}

function reviewDecision(record, status) {
  if (!record) return;
  if (status === 'APROBADO') {
    openModal('Aprobar registro', `<p>¿Confirmas la aprobación de <b>${escapeHtml(recordLabel(record._collection))}</b> para el activo <b>${escapeHtml(record.codigoActivo)}</b>?</p><div class="modal-actions"><button class="btn btn-outline" data-modal-close>Cancelar</button><button class="btn btn-success" id="confirm-review">Aprobar registro</button></div>`, { onOpen: root => { root.querySelector('#confirm-review').onclick = event => performReview(record, status, '', event.currentTarget); } }); return;
  }
  openModal('Devolver para corrección', `<p>Indica claramente qué debe corregir el usuario en el registro de <b>${escapeHtml(record.codigoActivo)}</b>.</p><label class="field"><span>Motivo de devolución <b>*</b></span><textarea id="return-reason" rows="4" placeholder="Ej. Completar la orden de trabajo y verificar el kilometraje."></textarea></label><div class="modal-actions"><button class="btn btn-outline" data-modal-close>Cancelar</button><button class="btn btn-danger" id="confirm-review">Devolver registro</button></div>`, { onOpen: root => { root.querySelector('#confirm-review').onclick = event => performReview(record, status, root.querySelector('#return-reason').value, event.currentTarget); } });
}

async function performReview(record, status, reason, button) {
  if (status === 'DEVUELTO' && !String(reason).trim()) return toast('Debes indicar el motivo de devolución.', 'error');
  setBusy(button, true, 'Actualizando…');
  try { await service.reviewRecord(record._collection, record.id, status, reason); closeModal(); toast(status === 'APROBADO' ? 'El registro fue aprobado.' : 'El registro fue devuelto para corrección.'); pageReview(); }
  catch (error) { toast(`No se pudo actualizar: ${error.message}`, 'error'); setBusy(button, false); }
}

async function pageReports() {
  const root = pageRoot(); const executive = state.page === 'Panel gerencial'; root.innerHTML = `${pageHeader()}${loader('Consolidando indicadores…')}`;
  const records = await service.listRecords(); state.reportRecords = records; state.filteredReportRecords = records;
  root.innerHTML = `${pageHeader()}<section id="report-summary"></section><section class="report-controls"><div class="section-heading"><div><h2>Filtros del reporte</h2><p>Combina criterios y exporta únicamente los resultados visibles.</p></div><div class="export-actions"><button class="btn btn-outline" id="print-report">Imprimir / PDF</button><button class="btn btn-primary" id="excel-report">Exportar Excel</button></div></div><div class="report-filter-grid"><label class="field"><span>Activo</span><input id="report-asset" placeholder="Código del activo"></label><label class="field"><span>Proyecto</span><input id="report-project" placeholder="Proyecto o campamento"></label><label class="field"><span>Técnico</span><input id="report-technician" placeholder="Nombre o correo"></label><label class="field"><span>Tipo de registro</span><select id="report-type"><option value="">Todos</option>${Object.entries(RECORD_LABELS).map(([value, label]) => `<option value="${value}">${escapeHtml(label)}</option>`).join('')}</select></label><label class="field"><span>Estado</span><select id="report-status"><option value="">Todos</option><option>BORRADOR</option><option>ENVIADO</option><option>APROBADO</option><option>DEVUELTO</option></select></label><label class="field"><span>Desde</span><input id="report-from" type="date"></label><label class="field"><span>Hasta</span><input id="report-to" type="date"></label><button class="btn btn-secondary filter-reset" id="report-reset">Limpiar filtros</button></div></section><section id="report-results"></section>`;
  const apply = () => {
    state.filteredReportRecords = filterRecords(records, { activo: document.querySelector('#report-asset').value, proyecto: document.querySelector('#report-project').value, tecnico: document.querySelector('#report-technician').value, tipo: document.querySelector('#report-type').value, estado: document.querySelector('#report-status').value, fechaDesde: document.querySelector('#report-from').value, fechaHasta: document.querySelector('#report-to').value }); drawReport(executive);
  };
  document.querySelectorAll('.report-filter-grid input,.report-filter-grid select').forEach(input => input.addEventListener('input', apply));
  document.querySelector('#report-reset').onclick = () => { document.querySelectorAll('.report-filter-grid input,.report-filter-grid select').forEach(input => { input.value = ''; }); apply(); };
  document.querySelector('#excel-report').onclick = () => downloadExcel(state.filteredReportRecords);
  document.querySelector('#print-report').onclick = () => { try { printReport(state.filteredReportRecords, executive ? 'Panel gerencial MEV' : 'Reporte consolidado MEV'); } catch (error) { toast(error.message, 'error'); } };
  drawReport(executive);
}

function drawReport(executive) {
  const records = state.filteredReportRecords; const summary = calculateSummary(records);
  document.querySelector('#report-summary').innerHTML = `<div class="kpi-grid report-kpis">${kpiCard('Mantenimientos', summary.totalMantenimientos, '⚙', 'teal')}${kpiCard('Tomas de huella', summary.totalHuellas, '◎', 'blue')}${kpiCard('Huellas críticas', summary.huellasCriticas, '!', summary.huellasCriticas ? 'red' : 'green')}${kpiCard('Intervenciones', summary.totalIntervenciones, '↻', 'amber')}${kpiCard('Pendientes', summary.enviados, '◷', 'amber')}${kpiCard('Aprobados', summary.aprobados, '✓', 'green')}</div><div class="chart-grid">${statusChart(summary)}${serviceChart(summary)}</div>`;
  document.querySelector('#report-results').innerHTML = `<div class="section-heading report-result-heading"><div><h2>${executive ? 'Detalle ejecutivo' : 'Registros encontrados'}</h2><p>${records.length} registro${records.length === 1 ? '' : 's'} con los filtros actuales.</p></div></div>${records.length ? recordsTable(records, 'history') : emptyState('Sin resultados', 'Modifica o limpia los filtros para visualizar información.')}`; bindTableActions(records, 'history');
}

function statusChart(summary) {
  const values = [['Borradores', summary.borradores, 'gray'], ['Pendientes', summary.enviados, 'amber'], ['Aprobados', summary.aprobados, 'green'], ['Devueltos', summary.devueltos, 'red']]; const max = Math.max(1, ...values.map(([, value]) => value));
  return `<article class="chart-card"><div><h3>Registros por estado</h3><p>Distribución del flujo de revisión.</p></div><div class="bar-chart">${values.map(([label, value, color]) => `<div class="bar-row"><span>${label}</span><div><i class="${color}" style="width:${(value / max) * 100}%"></i></div><b>${value}</b></div>`).join('')}</div></article>`;
}

function serviceChart(summary) {
  const total = Math.max(1, summary.preventivos + summary.correctivos);
  return `<article class="chart-card"><div><h3>Tipo de mantenimiento</h3><p>Preventivo frente a correctivo.</p></div><div class="donut-layout"><div class="donut" style="--preventive:${(summary.preventivos / total) * 100}%;"><span>${summary.totalMantenimientos}<small>Total</small></span></div><div class="legend"><span><i class="teal-dot"></i>Preventivos <b>${summary.preventivos}</b></span><span><i class="amber-dot"></i>Correctivos <b>${summary.correctivos}</b></span></div></div></article>`;
}

async function pageMatrix() {
  const root = pageRoot(); const assets = service.assets;
  const types = [...new Set(assets.map(asset => asset.subtipo || asset.tipo).filter(Boolean))].sort();
  const locations = [...new Set(assets.map(asset => asset.ubicacionActual).filter(Boolean))].sort();
  root.innerHTML = `${pageHeader()}<div class="matrix-toolbar"><label class="search-box"><span>⌕</span><input id="matrix-search" placeholder="Código, marca, modelo o serie…"></label><label><span>Tipo</span><select id="matrix-type"><option value="">Todos</option>${types.map(value => `<option>${escapeHtml(value)}</option>`).join('')}</select></label><label><span>Ubicación</span><select id="matrix-location"><option value="">Todas</option>${locations.map(value => `<option>${escapeHtml(value)}</option>`).join('')}</select></label><label><span>Estado</span><select id="matrix-status"><option value="">Todos</option><option>OPERATIVA</option><option>INOPERATIVA</option><option>EN_REPARACION</option></select></label></div><div id="matrix-results"></div>`;
  const draw = () => {
    const query = normalizeText(document.querySelector('#matrix-search').value); const type = document.querySelector('#matrix-type').value; const location = document.querySelector('#matrix-location').value; const status = document.querySelector('#matrix-status').value;
    const filtered = assets.filter(asset => (!query || normalizeText([asset.codigo, asset.marca, asset.modelo, asset.serie, asset.matricula].join(' ')).includes(query)) && (!type || (asset.subtipo || asset.tipo) === type) && (!location || asset.ubicacionActual === location) && (!status || normalizeUpper(asset.status) === status));
    const pageSize = 40; const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize)); state.matrixPage = Math.min(state.matrixPage, pageCount); const pageRows = filtered.slice((state.matrixPage - 1) * pageSize, state.matrixPage * pageSize);
    document.querySelector('#matrix-results').innerHTML = `<div class="matrix-summary"><span><b>${filtered.length}</b> activos encontrados</span><span>Página ${state.matrixPage} de ${pageCount}</span></div>${pageRows.length ? matrixTable(pageRows) : emptyState('Sin activos', 'No existen coincidencias con los filtros seleccionados.')}<div class="pagination"><button class="btn btn-outline" id="matrix-prev" ${state.matrixPage <= 1 ? 'disabled' : ''}>Anterior</button><button class="btn btn-outline" id="matrix-next" ${state.matrixPage >= pageCount ? 'disabled' : ''}>Siguiente</button></div>`;
    const map = new Map(pageRows.map(asset => [asset.codigo, asset])); document.querySelectorAll('[data-asset-code]').forEach(button => button.onclick = () => showAssetDetails(map.get(button.dataset.assetCode)));
    document.querySelector('#matrix-prev').onclick = () => { state.matrixPage -= 1; draw(); }; document.querySelector('#matrix-next').onclick = () => { state.matrixPage += 1; draw(); };
  };
  document.querySelectorAll('.matrix-toolbar input,.matrix-toolbar select').forEach(input => input.addEventListener('input', () => { state.matrixPage = 1; draw(); })); draw();
}

function matrixTable(assets) {
  return `<div class="table-card"><div class="table-scroll"><table><thead><tr><th>Código</th><th>Tipo / subtipo</th><th>Marca y modelo</th><th>Indicador</th><th>Ubicación</th><th>Estado</th><th>Configuración</th><th></th></tr></thead><tbody>${assets.map(asset => `<tr><td><b>${escapeHtml(asset.codigo)}</b><small>${escapeHtml(asset.matricula || asset.serie || '')}</small></td><td>${escapeHtml(asset.subtipo || asset.tipo || '—')}<small>${escapeHtml(asset.tipo || '')}</small></td><td>${escapeHtml(asset.marca || '—')}<small>${escapeHtml(asset.modelo || '')}</small></td><td>${escapeHtml(asset.indicador || 'N/A')}<small>Intervalo: ${escapeHtml(asset.intervalo || '—')}</small></td><td>${escapeHtml(asset.ubicacionActual || '—')}</td><td>${statusBadge(asset.status || 'SIN ESTADO')}</td><td>${assetPositionCount(asset)} posiciones<small>${escapeHtml(asset.configuracionRuedas || '')}</small></td><td><button class="btn-icon" data-asset-code="${escapeHtml(asset.codigo)}">Detalle</button></td></tr>`).join('')}</tbody></table></div></div>`;
}

function showAssetDetails(asset) {
  if (!asset) return; const canEdit = profile().rol === 'PLANIFICADOR';
  const fields = [['Código', asset.codigo], ['Tipo', asset.tipo], ['Subtipo', asset.subtipo], ['Marca', asset.marca], ['Modelo', asset.modelo], ['Serie', asset.serie], ['Matrícula', asset.matricula], ['Año', asset.anioFabricacion], ['Indicador', asset.indicador], ['Intervalo', asset.intervalo], ['Ubicación', asset.ubicacionActual], ['Estado', asset.status], ['Configuración', asset.configuracionRuedas], ['Posiciones', assetPositionCount(asset)], ['Kilometraje', asset.kilometraje], ['Horómetro', asset.horometro]];
  openModal(`Activo ${asset.codigo}`, `<div class="detail-grid">${fields.filter(([, value]) => value !== '' && value !== null && value !== undefined).map(([label, value]) => `<div class="detail-item"><span>${escapeHtml(label)}</span><b>${escapeHtml(value)}</b></div>`).join('')}</div><div class="permission-grid"><span class="${assetAllows(asset, 'permitePreventivo') ? 'enabled' : ''}">Preventivo</span><span class="${assetAllows(asset, 'permiteCorrectivo') ? 'enabled' : ''}">Correctivo</span><span class="${assetAllows(asset, 'permiteTomaHuella') ? 'enabled' : ''}">Toma de huella</span><span class="${assetAllows(asset, 'permiteIntervencionLlanta') ? 'enabled' : ''}">Intervención</span></div><div class="modal-actions"><button class="btn btn-outline" data-modal-close>Cerrar</button>${canEdit ? '<button class="btn btn-primary" id="edit-asset">Editar configuración</button>' : ''}</div>`, { wide: true, onOpen: root => root.querySelector('#edit-asset')?.addEventListener('click', () => editAssetModal(asset)) });
}

function editAssetModal(asset) {
  openModal(`Editar configuración · ${asset.codigo}`, `<form id="asset-edit-form"><div class="form-grid"><label class="field"><span>Ubicación</span><input name="ubicacionActual" value="${escapeHtml(asset.ubicacionActual || '')}"></label><label class="field"><span>Estado</span><select name="status"><option value="OPERATIVA" ${asset.status === 'OPERATIVA' ? 'selected' : ''}>Operativa</option><option value="INOPERATIVA" ${asset.status === 'INOPERATIVA' ? 'selected' : ''}>Inoperativa</option><option value="EN_REPARACION" ${asset.status === 'EN_REPARACION' ? 'selected' : ''}>En reparación</option></select></label><label class="field"><span>Intervalo</span><input name="intervalo" type="number" min="0" value="${escapeHtml(asset.intervalo || 0)}"></label><label class="field"><span>Posiciones de llanta</span><input name="cantidadPosiciones" type="number" min="0" max="12" step="1" value="${assetPositionCount(asset)}"><small>Controla automáticamente las opciones P1–PN en toma de huella e intervención.</small></label></div><div class="check-grid"><label><input type="checkbox" name="permitePreventivo" ${assetAllows(asset, 'permitePreventivo') ? 'checked' : ''}> Mantenimiento preventivo</label><label><input type="checkbox" name="permiteCorrectivo" ${assetAllows(asset, 'permiteCorrectivo') ? 'checked' : ''}> Mantenimiento correctivo</label><label><input type="checkbox" name="permiteTomaHuella" ${assetAllows(asset, 'permiteTomaHuella') ? 'checked' : ''}> Toma de huella</label><label><input type="checkbox" name="permiteIntervencionLlanta" ${assetAllows(asset, 'permiteIntervencionLlanta') ? 'checked' : ''}> Intervención de llanta</label></div><div class="modal-actions"><button type="button" class="btn btn-outline" data-modal-close>Cancelar</button><button type="submit" class="btn btn-primary">Guardar configuración</button></div></form>`, { onOpen: root => {
    root.querySelector('#asset-edit-form').onsubmit = async event => {
      event.preventDefault(); const button = event.submitter; const values = new FormData(event.currentTarget); setBusy(button, true, 'Guardando…');
      try {
        await service.updateAsset(asset, { ubicacionActual: values.get('ubicacionActual'), status: values.get('status'), intervalo: values.get('intervalo'), cantidadPosiciones: values.get('cantidadPosiciones'), permitePreventivo: values.has('permitePreventivo'), permiteCorrectivo: values.has('permiteCorrectivo'), permiteTomaHuella: values.has('permiteTomaHuella'), permiteIntervencionLlanta: values.has('permiteIntervencionLlanta') });
        const positions = assetPositionCount(selectedAsset(asset.codigo));
        closeModal(); toast(`Configuración actualizada: los formularios usarán ${positions} posición${positions === 1 ? '' : 'es'} de llanta.`); pageMatrix();
      } catch (error) { toast(error.message, 'error'); setBusy(button, false); }
    };
  }});
}

async function pageUsers() {
  const root = pageRoot(); root.innerHTML = `${pageHeader()}${loader('Cargando usuarios…')}`; const users = await service.listUsers();
  const modeNote = service.mode === 'firebase'
    ? 'Crea la credencial de acceso y su perfil, edita los datos, asigna roles, activa o inactiva cuentas y envía recuperación de contraseña.'
    : 'Los cambios se guardan únicamente en este navegador. La contraseña no se utiliza en el modo demostración.';
  root.innerHTML = `${pageHeader()}<div class="inline-alert info"><b>Administración de accesos:</b> ${escapeHtml(modeNote)}</div><div class="toolbar user-toolbar"><label class="search-box"><span>⌕</span><input id="user-search" placeholder="Buscar nombre, correo o cargo…"></label><button class="btn btn-primary" id="create-user">＋ Crear usuario</button><div class="toolbar-summary"><b>${users.length}</b> usuarios</div></div><div id="user-results">${usersTable(users)}</div>`;
  const draw = filtered => { document.querySelector('#user-results').innerHTML = usersTable(filtered); bindUserActions(filtered); };
  document.querySelector('#user-search').oninput = event => { const query = normalizeText(event.target.value); draw(users.filter(item => normalizeText(`${item.nombres} ${item.apellidos} ${item.email} ${item.cargo} ${item.rol}`).includes(query))); };
  document.querySelector('#create-user').onclick = createUserModal;
  bindUserActions(users);
}

function usersTable(users) {
  if (!users.length) return emptyState('Sin usuarios', 'No existen perfiles para mostrar.');
  return `<div class="table-card"><div class="table-scroll"><table><thead><tr><th>Usuario</th><th>Cargo</th><th>Rol</th><th>Estado</th><th>Acciones</th></tr></thead><tbody>${users.map(item => {
    const id = item.id || item.uid; const isCurrent = item.uid === user().uid;
    return `<tr><td><div class="person-cell"><div class="avatar small">${escapeHtml(initials(`${item.nombres} ${item.apellidos}`))}</div><div><b>${escapeHtml(`${item.nombres || ''} ${item.apellidos || ''}`.trim() || 'Sin nombre')}</b><small>${escapeHtml(item.email || '')}${isCurrent ? ' · Sesión actual' : ''}</small></div></div></td><td>${escapeHtml(item.cargo || '—')}</td><td><select class="table-select" data-role-id="${escapeHtml(id)}" ${isCurrent ? 'disabled title="El rol de la sesión actual no se puede cambiar"' : ''}>${ROLES.map(role => `<option value="${role}" ${role === normalizeUpper(item.rol) ? 'selected' : ''}>${escapeHtml(roleLabel(role))}</option>`).join('')}</select></td><td><select class="table-select" data-status-id="${escapeHtml(id)}" ${isCurrent ? 'disabled title="La sesión actual no se puede inactivar"' : ''}><option value="ACTIVO" ${normalizeUpper(item.estadoUsuario) === 'ACTIVO' ? 'selected' : ''}>Activo</option><option value="INACTIVO" ${normalizeUpper(item.estadoUsuario) === 'INACTIVO' ? 'selected' : ''}>Inactivo</option></select></td><td class="table-actions"><button class="btn btn-primary btn-small" data-save-user="${escapeHtml(id)}" ${isCurrent ? 'disabled' : ''}>Guardar permisos</button><button class="btn-icon" data-edit-user="${escapeHtml(id)}">Editar datos</button>${service.mode === 'firebase' ? `<button class="btn-icon" data-reset-user="${escapeHtml(id)}" ${item.email ? '' : 'disabled'}>Recuperar clave</button>` : ''}</td></tr>`;
  }).join('')}</tbody></table></div></div>`;
}

function bindUserActions(users) {
  document.querySelectorAll('[data-save-user]').forEach(button => button.onclick = async () => {
    const id = button.dataset.saveUser; const role = document.querySelector(`[data-role-id="${CSS.escape(id)}"]`).value; const status = document.querySelector(`[data-status-id="${CSS.escape(id)}"]`).value; const current = users.find(item => (item.id || item.uid) === id);
    if (current?.uid === user().uid && status === 'INACTIVO') return toast('No puedes inactivar tu propia sesión.', 'error');
    setBusy(button, true, 'Guardando…');
    try { await service.updateUser(id, { rol: role, estadoUsuario: status }); toast('Los permisos del usuario fueron actualizados.'); await pageUsers(); }
    catch (error) { toast(error.message, 'error'); setBusy(button, false); }
  });
  document.querySelectorAll('[data-edit-user]').forEach(button => button.onclick = () => {
    const current = users.find(item => (item.id || item.uid) === button.dataset.editUser);
    if (current) editUserModal(current);
  });
  document.querySelectorAll('[data-reset-user]').forEach(button => button.onclick = () => {
    const current = users.find(item => (item.id || item.uid) === button.dataset.resetUser);
    if (current) resetUserPasswordModal(current);
  });
}

function createUserModal() {
  const requiresPassword = service.mode === 'firebase';
  openModal('Crear usuario', `<form id="user-create-form"><div class="form-grid"><label class="field"><span>Nombres <b>*</b></span><input name="nombres" autocomplete="given-name" required></label><label class="field"><span>Apellidos <b>*</b></span><input name="apellidos" autocomplete="family-name" required></label><label class="field field-full"><span>Correo de acceso <b>*</b></span><input name="email" type="email" autocomplete="email" placeholder="usuario@empresa.com" required></label><label class="field"><span>Cargo <b>*</b></span><input name="cargo" placeholder="Ej. Técnico mecánico" required></label><label class="field"><span>Rol <b>*</b></span><select name="rol">${ROLES.map(role => `<option value="${role}">${escapeHtml(roleLabel(role))}</option>`).join('')}</select></label><label class="field"><span>Estado</span><select name="estadoUsuario"><option value="ACTIVO">Activo</option><option value="INACTIVO">Inactivo</option></select></label>${requiresPassword ? `<label class="field"><span>Contraseña temporal <b>*</b></span><input name="password" type="password" minlength="6" autocomplete="new-password" required><small>Mínimo 6 caracteres; no se guarda en el perfil.</small></label><label class="field"><span>Confirmar contraseña <b>*</b></span><input name="confirmPassword" type="password" minlength="6" autocomplete="new-password" required></label>` : '<div class="inline-alert warning field-full">En demostración se crea el perfil local, no una credencial real de Firebase.</div>'}</div><div class="modal-actions"><button type="button" class="btn btn-outline" data-modal-close>Cancelar</button><button type="submit" class="btn btn-primary">Crear usuario</button></div></form>`, { wide: true, onOpen: root => {
    root.querySelector('#user-create-form').onsubmit = async event => {
      event.preventDefault(); const button = event.submitter; const values = new FormData(event.currentTarget);
      if (requiresPassword && values.get('password') !== values.get('confirmPassword')) return toast('Las contraseñas no coinciden.', 'error');
      setBusy(button, true, 'Creando…');
      try {
        await service.createUser({
          nombres: values.get('nombres'), apellidos: values.get('apellidos'), email: values.get('email'), cargo: values.get('cargo'),
          rol: values.get('rol'), estadoUsuario: values.get('estadoUsuario'), password: values.get('password')
        });
        closeModal(); toast(requiresPassword ? 'Usuario creado. Ya puede iniciar sesión con su correo y contraseña temporal.' : 'Usuario creado en el modo demostración.'); await pageUsers();
      } catch (error) { toast(error.message, 'error', 7000); setBusy(button, false); }
    };
  }});
}

function editUserModal(item) {
  const id = item.id || item.uid;
  openModal('Editar datos del usuario', `<form id="user-edit-form"><div class="form-grid"><label class="field"><span>Nombres <b>*</b></span><input name="nombres" value="${escapeHtml(item.nombres || '')}" required></label><label class="field"><span>Apellidos <b>*</b></span><input name="apellidos" value="${escapeHtml(item.apellidos || '')}" required></label><label class="field field-full"><span>Correo de acceso</span><input value="${escapeHtml(item.email || '')}" disabled><small>El correo de autenticación no se modifica desde esta pantalla.</small></label><label class="field field-full"><span>Cargo <b>*</b></span><input name="cargo" value="${escapeHtml(item.cargo || '')}" required></label></div><div class="modal-actions"><button type="button" class="btn btn-outline" data-modal-close>Cancelar</button><button type="submit" class="btn btn-primary">Guardar datos</button></div></form>`, { onOpen: root => {
    root.querySelector('#user-edit-form').onsubmit = async event => {
      event.preventDefault(); const button = event.submitter; const values = new FormData(event.currentTarget); setBusy(button, true, 'Guardando…');
      try {
        await service.updateUser(id, { nombres: values.get('nombres'), apellidos: values.get('apellidos'), cargo: values.get('cargo'), rol: item.rol, estadoUsuario: item.estadoUsuario });
        closeModal(); toast('Los datos del usuario fueron actualizados.'); await pageUsers();
      } catch (error) { toast(error.message, 'error'); setBusy(button, false); }
    };
  }});
}

function resetUserPasswordModal(item) {
  openModal('Enviar recuperación de contraseña', `<p>Firebase enviará un enlace de recuperación a <b>${escapeHtml(item.email || '')}</b>. La aplicación no podrá ver ni modificar la nueva contraseña.</p><div class="modal-actions"><button class="btn btn-outline" data-modal-close>Cancelar</button><button class="btn btn-primary" id="confirm-reset-user">Enviar enlace</button></div>`, { onOpen: root => {
    root.querySelector('#confirm-reset-user').onclick = async event => {
      const button = event.currentTarget; setBusy(button, true, 'Enviando…');
      try { await service.resetPassword(item.email); closeModal(); toast('Se envió el enlace de recuperación.'); }
      catch (error) { toast(error.message, 'error'); setBusy(button, false); }
    };
  }});
}

renderBoot();
await service.init((session, error = '') => {
  state.session = session; state.page = 'Inicio'; state.editing = null;
  if (session) renderShell(); else renderLogin(error);
});
if (!state.session) renderLogin(service.firebaseError ? 'No se pudo conectar con Firebase. El modo demostración continúa disponible.' : '');

if ('serviceWorker' in navigator && location.protocol !== 'file:') {
  window.addEventListener('load', () => navigator.serviceWorker.register('/service-worker.js').catch(() => {}));
}
