import {
  COLLECTIONS,
  ROLES,
  normalizeUpper,
  sortRecords
} from './core.js';

const FIREBASE_VERSION = '12.2.1';
const FIREBASE_CONFIG = Object.freeze({
  apiKey: 'AIzaSyDiM5VHKnim9ONi7UoUwGihCJjDjR7EZUc',
  authDomain: 'mev-mantenimiento.firebaseapp.com',
  projectId: 'mev-mantenimiento',
  storageBucket: 'mev-mantenimiento.firebasestorage.app',
  messagingSenderId: '213852784130'
});

const LOCAL_DB_KEY = 'mev_mantenimiento_web_demo_v2';
const DEMO_UID = 'demo-planificador';

function nowIso() {
  return new Date().toISOString();
}

function createId(prefix = 'registro') {
  if (globalThis.crypto?.randomUUID) return `${prefix}-${crypto.randomUUID()}`;
  return `${prefix}-${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function clone(value) {
  return JSON.parse(JSON.stringify(value));
}

function userManagementError(error) {
  const code = String(error?.code || '');
  if (/email-already-in-use/.test(code)) return 'Ya existe una credencial con ese correo electrónico.';
  if (/invalid-email/.test(code)) return 'El correo electrónico no tiene un formato válido.';
  if (/weak-password/.test(code)) return 'La contraseña temporal debe tener al menos 6 caracteres.';
  if (/permission-denied/.test(code)) return 'Firebase rechazó la operación. Publica las reglas incluidas en la carpeta firebase antes de crear perfiles.';
  if (/network-request-failed/.test(code)) return 'No se pudo conectar con Firebase para crear el usuario.';
  return error?.message || 'No se pudo completar la administración del usuario.';
}

function seedDatabase() {
  const date = new Date();
  const previous = new Date(date.getTime() - 86400000).toISOString();
  const weekAgo = new Date(date.getTime() - 6 * 86400000).toISOString();
  return {
    [COLLECTIONS.maintenance]: [
      {
        id: 'demo-mantenimiento-1', codigoActivo: 'EVOLQ0087', tipoServicio: 'PREVENTIVO',
        kilometraje: 84520, horometro: null, accionEjecutada: 'Cambio de aceite y filtros; inspección general del sistema de frenos.',
        observaciones: 'Unidad operativa.', ordenTrabajo: 'OT-2026-184', numeroPedido: 'PED-791',
        estadoRegistro: 'APROBADO', uidUsuario: DEMO_UID, emailUsuario: 'luis.demo@mev.local',
        fechaCreacion: weekAgo, fechaActualizacion: previous, fechaAprobacion: previous
      },
      {
        id: 'demo-mantenimiento-2', codigoActivo: 'ECAMI0041', tipoServicio: 'CORRECTIVO',
        kilometraje: 112400, horometro: null, accionEjecutada: 'Diagnóstico de pérdida de potencia y limpieza de sensores.',
        observaciones: 'Pendiente de prueba en ruta.', ordenTrabajo: 'OT-2026-191', numeroPedido: '',
        estadoRegistro: 'ENVIADO', uidUsuario: DEMO_UID, emailUsuario: 'luis.demo@mev.local',
        fechaCreacion: previous, fechaActualizacion: previous, fechaEnvio: previous
      },
      {
        id: 'demo-mantenimiento-3', codigoActivo: 'EEXCA0029', tipoServicio: 'CORRECTIVO',
        kilometraje: null, horometro: 6341, accionEjecutada: 'Revisión de fuga hidráulica.',
        observaciones: '', ordenTrabajo: '', numeroPedido: '', estadoRegistro: 'BORRADOR',
        uidUsuario: DEMO_UID, emailUsuario: 'luis.demo@mev.local', fechaCreacion: nowIso(), fechaActualizacion: nowIso()
      }
    ],
    [COLLECTIONS.tread]: [
      {
        id: 'demo-huella-1', codigoActivo: 'EVOLQ0087', proyecto: 'LOJA VÍA ANTIGUA', kilometraje: 84520,
        horometro: null, P1: 9.2, P2: 9.1, P3: 7.4, P4: 7.2, P5: 6.1, P6: 6.0,
        P7: 5.8, P8: 5.9, P9: 7.0, P10: 7.1, huellas: [9.2, 9.1, 7.4, 7.2, 6.1, 6.0, 5.8, 5.9, 7.0, 7.1],
        estadoGeneral: 'REGULAR', novedad: 'P7 y P8 próximas al límite de intervención.', nombreTecnico: 'Luis Barragán',
        estadoRegistro: 'APROBADO', uidUsuario: DEMO_UID, emailUsuario: 'luis.demo@mev.local',
        fechaCreacion: weekAgo, fechaActualizacion: previous, fechaAprobacion: previous
      },
      {
        id: 'demo-huella-2', codigoActivo: 'ERODOT0010', proyecto: 'CATAMAYO', horometro: 416, kilometraje: null,
        P1: 8.4, P2: 8.3, huellas: [8.4, 8.3], estadoGeneral: 'BUENO', novedad: '',
        nombreTecnico: 'Vulcanizador Demo', estadoRegistro: 'ENVIADO', uidUsuario: 'demo-vulcanizador',
        emailUsuario: 'vulcanizador.demo@mev.local', fechaCreacion: previous, fechaActualizacion: previous, fechaEnvio: previous
      }
    ],
    [COLLECTIONS.tire]: [
      {
        id: 'demo-intervencion-1', codigoActivo: 'EVOLQ0087', proyecto: 'LOJA VÍA ANTIGUA', kilometraje: 84520,
        horometro: null, tipoIntervencion: 'ROTACION', posicion: 'P7', huella: 5.8,
        marcaLlanta: 'BRIDGESTONE', medidaLlanta: '12R22.5', serieLlanta: 'DEMO-2281',
        motivo: 'Desgaste irregular', observaciones: 'Rotación P7 a P9.', nombreTecnico: 'Vulcanizador Demo',
        estadoRegistro: 'DEVUELTO', motivoDevolucion: 'Completar el número de orden de trabajo en observaciones.',
        uidUsuario: DEMO_UID, emailUsuario: 'luis.demo@mev.local', fechaCreacion: previous, fechaActualizacion: nowIso()
      }
    ],
    usuarios: ROLES.map((role, index) => ({
      id: `demo-user-${index + 1}`,
      uid: index === 5 ? DEMO_UID : `demo-${role.toLowerCase()}`,
      nombres: index === 5 ? 'Luis' : role.split('_')[0][0] + role.split('_')[0].slice(1).toLowerCase(),
      apellidos: index === 5 ? 'Barragán' : 'Demostración',
      email: index === 5 ? 'luis.demo@mev.local' : `${role.toLowerCase()}@mev.local`,
      cargo: role.replaceAll('_', ' '), rol: role, estadoUsuario: 'ACTIVO'
    })),
    assetOverrides: {}
  };
}

export class DataService {
  constructor() {
    this.mode = 'none';
    this.firebaseAvailable = false;
    this.firebaseError = '';
    this.firebase = null;
    this.appApi = null;
    this.auth = null;
    this.db = null;
    this.storage = null;
    this.session = null;
    this.localAssets = [];
    this.assets = [];
    this.onSessionChange = () => {};
  }

  async init(onSessionChange) {
    this.onSessionChange = onSessionChange || (() => {});
    await this.loadLocalAssets();
    try {
      const [appApi, authApi, firestoreApi, storageApi] = await Promise.all([
        import(`https://www.gstatic.com/firebasejs/${FIREBASE_VERSION}/firebase-app.js`),
        import(`https://www.gstatic.com/firebasejs/${FIREBASE_VERSION}/firebase-auth.js`),
        import(`https://www.gstatic.com/firebasejs/${FIREBASE_VERSION}/firebase-firestore.js`),
        import(`https://www.gstatic.com/firebasejs/${FIREBASE_VERSION}/firebase-storage.js`)
      ]);
      const firebaseApp = appApi.initializeApp(FIREBASE_CONFIG);
      this.appApi = appApi;
      this.firebase = { ...authApi, ...firestoreApi, ...storageApi };
      this.auth = authApi.getAuth(firebaseApp);
      this.db = firestoreApi.getFirestore(firebaseApp);
      this.storage = storageApi.getStorage(firebaseApp);
      this.firebaseAvailable = true;
      authApi.onAuthStateChanged(this.auth, user => this.handleFirebaseSession(user));
    } catch (error) {
      this.firebaseAvailable = false;
      this.firebaseError = error?.message || 'No se pudo conectar con Firebase.';
      this.onSessionChange(null);
    }
  }

  async handleFirebaseSession(user) {
    if (!user) {
      if (this.mode !== 'demo') {
        this.mode = 'none';
        this.session = null;
        this.onSessionChange(null);
      }
      return;
    }
    try {
      const profileSnapshot = await this.firebase.getDoc(this.firebase.doc(this.db, 'usuarios', user.uid));
      if (!profileSnapshot.exists()) throw new Error('El usuario no tiene un perfil registrado.');
      const profile = { uid: user.uid, ...profileSnapshot.data() };
      if (normalizeUpper(profile.estadoUsuario) !== 'ACTIVO') throw new Error('El perfil de usuario está inactivo.');
      if (!ROLES.includes(normalizeUpper(profile.rol))) throw new Error('El perfil no tiene un rol válido.');
      this.mode = 'firebase';
      this.session = { user, profile: { ...profile, rol: normalizeUpper(profile.rol) } };
      await this.loadAssets();
      this.onSessionChange(this.session);
    } catch (error) {
      this.firebaseError = error?.message || 'No se pudo consultar el perfil.';
      await this.firebase.signOut(this.auth);
      this.onSessionChange(null, this.firebaseError);
    }
  }

  async loadLocalAssets() {
    try {
      const response = await fetch('/inventario_activos_firestore.json', { cache: 'no-store' });
      if (!response.ok) throw new Error('No se encontró el catálogo local.');
      this.localAssets = await response.json();
    } catch {
      this.localAssets = [];
    }
    this.assets = this.applyAssetOverrides(this.localAssets);
    return this.assets;
  }

  async loadAssets() {
    if (this.mode !== 'firebase') {
      this.assets = this.applyAssetOverrides(this.localAssets);
      return this.assets;
    }
    try {
      const snapshot = await this.firebase.getDocs(this.firebase.collection(this.db, 'activos'));
      const online = snapshot.docs.map(item => ({ id: item.id, ...item.data() }));
      this.assets = online.length ? online : this.localAssets;
    } catch {
      this.assets = this.localAssets;
    }
    return this.assets;
  }

  applyAssetOverrides(assets) {
    const overrides = this.readLocalDatabase().assetOverrides || {};
    return assets.map(asset => ({ ...asset, ...(overrides[asset.codigo] || {}) }));
  }

  connectionInfo() {
    return {
      mode: this.mode,
      firebaseAvailable: this.firebaseAvailable,
      firebaseError: this.firebaseError,
      online: navigator.onLine
    };
  }

  async signIn(email, password) {
    if (!this.firebaseAvailable || !this.auth) throw new Error('Firebase no está disponible. Usa el modo demostración.');
    await this.firebase.signInWithEmailAndPassword(this.auth, String(email).trim(), String(password));
  }

  async resetPassword(email) {
    if (!this.firebaseAvailable || !this.auth) throw new Error('Firebase no está disponible.');
    const normalized = String(email).trim();
    if (!normalized) throw new Error('Ingresa primero tu correo electrónico.');
    await this.firebase.sendPasswordResetEmail(this.auth, normalized);
  }

  async enterDemo(role = 'PLANIFICADOR') {
    const normalizedRole = ROLES.includes(role) ? role : 'PLANIFICADOR';
    const database = this.readLocalDatabase();
    const profile = database.usuarios.find(item => item.rol === normalizedRole) || database.usuarios[0];
    this.mode = 'demo';
    this.session = {
      user: { uid: profile.uid, email: profile.email, displayName: `${profile.nombres} ${profile.apellidos}` },
      profile: clone(profile)
    };
    this.assets = this.applyAssetOverrides(this.localAssets);
    this.onSessionChange(this.session);
    return this.session;
  }

  async signOut() {
    if (this.mode === 'firebase' && this.auth) await this.firebase.signOut(this.auth);
    this.mode = 'none';
    this.session = null;
    this.onSessionChange(null);
  }

  readLocalDatabase() {
    try {
      const stored = localStorage.getItem(LOCAL_DB_KEY);
      if (stored) {
        const parsed = JSON.parse(stored);
        if (parsed && typeof parsed === 'object') return parsed;
      }
    } catch {
      // A private browser window may block localStorage. Use an in-memory database.
    }
    if (!this.memoryDatabase) this.memoryDatabase = seedDatabase();
    const seeded = clone(this.memoryDatabase);
    this.writeLocalDatabase(seeded);
    return seeded;
  }

  writeLocalDatabase(database) {
    this.memoryDatabase = clone(database);
    try {
      localStorage.setItem(LOCAL_DB_KEY, JSON.stringify(database));
    } catch {
      // The in-memory copy remains available for the current presentation.
    }
  }

  resetDemoData() {
    const database = seedDatabase();
    this.writeLocalDatabase(database);
    this.assets = this.applyAssetOverrides(this.localAssets);
  }

  async listRecords({ ownOnly = false, statuses = null, collectionName = null } = {}) {
    const collections = collectionName ? [collectionName] : Object.values(COLLECTIONS);
    const records = [];
    if (this.mode === 'firebase') {
      for (const name of collections) {
        const snapshot = await this.firebase.getDocs(this.firebase.collection(this.db, name));
        snapshot.docs.forEach(item => records.push({ id: item.id, _collection: name, ...item.data() }));
      }
    } else {
      const database = this.readLocalDatabase();
      collections.forEach(name => (database[name] || []).forEach(item => records.push({ ...clone(item), _collection: name })));
    }
    const uid = this.session?.user?.uid;
    const statusSet = statuses ? new Set(statuses.map(normalizeUpper)) : null;
    return sortRecords(records.filter(record => {
      if (ownOnly && record.uidUsuario !== uid) return false;
      if (statusSet && !statusSet.has(normalizeUpper(record.estadoRegistro))) return false;
      return true;
    }));
  }

  async saveRecord(collectionName, data, { id = '', evidenceFile = null } = {}) {
    if (!Object.values(COLLECTIONS).includes(collectionName)) throw new Error('La colección indicada no es válida.');
    const status = normalizeUpper(data.estadoRegistro || 'BORRADOR');
    if (!['BORRADOR', 'ENVIADO'].includes(status)) throw new Error('El estado del registro no es válido.');
    const isEdit = Boolean(id);
    const user = this.session?.user;
    if (!user) throw new Error('No existe una sesión activa.');
    const timestamp = nowIso();
    const payload = {
      ...data,
      estadoRegistro: status,
      uidUsuario: data.uidUsuario || user.uid,
      emailUsuario: data.emailUsuario || user.email || '',
      fechaActualizacion: timestamp,
      ...(isEdit ? {} : { fechaCreacion: timestamp }),
      ...(status === 'ENVIADO' ? { fechaEnvio: timestamp } : {})
    };

    if (this.mode === 'firebase') {
      const cleanPayload = { ...payload, fechaActualizacion: this.firebase.serverTimestamp() };
      if (!isEdit) cleanPayload.fechaCreacion = this.firebase.serverTimestamp();
      if (status === 'ENVIADO') cleanPayload.fechaEnvio = this.firebase.serverTimestamp();
      if (status === 'ENVIADO' && isEdit) {
        cleanPayload.motivoDevolucion = this.firebase.deleteField();
        cleanPayload.fechaDevolucion = this.firebase.deleteField();
      }
      let recordId = id;
      if (isEdit) {
        await this.firebase.updateDoc(this.firebase.doc(this.db, collectionName, id), cleanPayload);
      } else {
        const reference = await this.firebase.addDoc(this.firebase.collection(this.db, collectionName), cleanPayload);
        recordId = reference.id;
      }
      let warning = '';
      if (evidenceFile) {
        try {
          const fileName = String(evidenceFile.name || 'evidencia.jpg').replace(/[^a-zA-Z0-9._-]/g, '_');
          const storageReference = this.firebase.ref(this.storage, `evidencias/${user.uid}/${collectionName}/${recordId}/${Date.now()}-${fileName}`);
          await this.firebase.uploadBytes(storageReference, evidenceFile, { contentType: evidenceFile.type || 'image/jpeg' });
          const photoUrl = await this.firebase.getDownloadURL(storageReference);
          await this.firebase.updateDoc(this.firebase.doc(this.db, collectionName, recordId), {
            fotoUrl: photoUrl,
            fotoNombre: fileName,
            fechaActualizacion: this.firebase.serverTimestamp()
          });
        } catch (error) {
          warning = `El registro se guardó, pero la evidencia no pudo cargarse: ${error.message}`;
        }
      }
      return { id: recordId, warning };
    }

    const database = this.readLocalDatabase();
    const target = database[collectionName] || (database[collectionName] = []);
    let recordId = id;
    if (evidenceFile) {
      payload.fotoDataUrl = await fileToDataUrl(evidenceFile);
      payload.fotoNombre = evidenceFile.name || 'evidencia.jpg';
    }
    if (isEdit) {
      const index = target.findIndex(item => item.id === id);
      if (index < 0) throw new Error('No se encontró el registro que deseas editar.');
      const original = target[index];
      target[index] = { ...original, ...clone(payload) };
      if (status === 'ENVIADO') {
        delete target[index].motivoDevolucion;
        delete target[index].fechaDevolucion;
      }
    } else {
      recordId = createId(collectionName);
      target.push({ id: recordId, ...clone(payload) });
    }
    this.writeLocalDatabase(database);
    return { id: recordId, warning: '' };
  }

  async reviewRecord(collectionName, id, newStatus, reason = '') {
    const status = normalizeUpper(newStatus);
    if (!['APROBADO', 'DEVUELTO'].includes(status)) throw new Error('La decisión de revisión no es válida.');
    if (status === 'DEVUELTO' && !String(reason).trim()) throw new Error('Debes indicar el motivo de devolución.');
    const user = this.session?.user;
    const timestamp = nowIso();
    if (this.mode === 'firebase') {
      const payload = {
        estadoRegistro: status,
        fechaRevision: this.firebase.serverTimestamp(),
        fechaActualizacion: this.firebase.serverTimestamp(),
        uidRevisor: user.uid,
        emailRevisor: user.email || ''
      };
      if (status === 'DEVUELTO') {
        payload.motivoDevolucion = String(reason).trim();
        payload.fechaDevolucion = this.firebase.serverTimestamp();
      } else {
        payload.motivoDevolucion = this.firebase.deleteField();
        payload.fechaDevolucion = this.firebase.deleteField();
        payload.fechaAprobacion = this.firebase.serverTimestamp();
      }
      await this.firebase.updateDoc(this.firebase.doc(this.db, collectionName, id), payload);
      return;
    }
    const database = this.readLocalDatabase();
    const target = database[collectionName] || [];
    const record = target.find(item => item.id === id);
    if (!record) throw new Error('No se encontró el registro.');
    Object.assign(record, {
      estadoRegistro: status, fechaRevision: timestamp, fechaActualizacion: timestamp,
      uidRevisor: user.uid, emailRevisor: user.email || ''
    });
    if (status === 'DEVUELTO') {
      record.motivoDevolucion = String(reason).trim();
      record.fechaDevolucion = timestamp;
    } else {
      delete record.motivoDevolucion;
      delete record.fechaDevolucion;
      record.fechaAprobacion = timestamp;
    }
    this.writeLocalDatabase(database);
  }

  async listUsers() {
    if (this.mode === 'firebase') {
      const snapshot = await this.firebase.getDocs(this.firebase.collection(this.db, 'usuarios'));
      return snapshot.docs.map(item => ({ id: item.id, uid: item.id, ...item.data() }))
        .sort((left, right) => String(left.nombres || left.email).localeCompare(String(right.nombres || right.email), 'es'));
    }
    return clone(this.readLocalDatabase().usuarios || [])
      .sort((left, right) => String(left.nombres || left.email).localeCompare(String(right.nombres || right.email), 'es'));
  }

  async createUser(data) {
    if (normalizeUpper(this.session?.profile?.rol) !== 'PLANIFICADOR') throw new Error('Solo el perfil PLANIFICADOR puede crear usuarios.');
    const profile = {
      nombres: String(data.nombres || '').trim(),
      apellidos: String(data.apellidos || '').trim(),
      email: String(data.email || '').trim().toLowerCase(),
      cargo: String(data.cargo || '').trim(),
      rol: normalizeUpper(data.rol),
      estadoUsuario: normalizeUpper(data.estadoUsuario || 'ACTIVO'),
      fechaCreacion: nowIso(),
      fechaActualizacion: nowIso()
    };
    const password = String(data.password || '');
    if (!profile.nombres || !profile.apellidos || !profile.email || !profile.cargo) throw new Error('Completa nombres, apellidos, correo y cargo.');
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(profile.email)) throw new Error('El correo electrónico no tiene un formato válido.');
    if (!ROLES.includes(profile.rol)) throw new Error('El rol seleccionado no es válido.');
    if (!['ACTIVO', 'INACTIVO'].includes(profile.estadoUsuario)) throw new Error('El estado seleccionado no es válido.');

    if (this.mode === 'firebase') {
      if (password.length < 6) throw new Error('La contraseña temporal debe tener al menos 6 caracteres.');
      let secondaryApp = null;
      let secondaryAuth = null;
      let credential = null;
      let profileCreated = false;
      try {
        secondaryApp = this.appApi.initializeApp(FIREBASE_CONFIG, `mev-user-admin-${Date.now()}-${Math.random().toString(16).slice(2)}`);
        secondaryAuth = this.firebase.getAuth(secondaryApp);
        credential = await this.firebase.createUserWithEmailAndPassword(secondaryAuth, profile.email, password);
        await this.firebase.updateProfile(credential.user, { displayName: `${profile.nombres} ${profile.apellidos}`.trim() });
        const onlineProfile = {
          ...profile,
          fechaCreacion: this.firebase.serverTimestamp(),
          fechaActualizacion: this.firebase.serverTimestamp()
        };
        await this.firebase.setDoc(this.firebase.doc(this.db, 'usuarios', credential.user.uid), onlineProfile);
        profileCreated = true;
        return { id: credential.user.uid, uid: credential.user.uid, ...profile };
      } catch (error) {
        if (credential?.user && !profileCreated) {
          try { await this.firebase.deleteUser(credential.user); } catch { /* La credencial puede requerir eliminación manual si Firebase ya cerró la sesión. */ }
        }
        throw new Error(userManagementError(error));
      } finally {
        if (secondaryAuth) {
          try { await this.firebase.signOut(secondaryAuth); } catch { /* La sesión secundaria ya puede estar cerrada. */ }
        }
        if (secondaryApp) {
          try { await this.appApi.deleteApp(secondaryApp); } catch { /* La aplicación secundaria se desecha al recargar. */ }
        }
      }
    }

    const database = this.readLocalDatabase();
    database.usuarios ||= [];
    if (database.usuarios.some(item => String(item.email).toLowerCase() === profile.email)) throw new Error('Ya existe un usuario con ese correo electrónico.');
    const uid = createId('demo-user');
    const created = { id: uid, uid, ...profile };
    database.usuarios.push(created);
    this.writeLocalDatabase(database);
    return clone(created);
  }

  async updateUser(id, changes) {
    const payload = {
      rol: normalizeUpper(changes.rol),
      estadoUsuario: normalizeUpper(changes.estadoUsuario),
      fechaActualizacion: nowIso()
    };
    if (Object.hasOwn(changes, 'nombres')) payload.nombres = String(changes.nombres || '').trim();
    if (Object.hasOwn(changes, 'apellidos')) payload.apellidos = String(changes.apellidos || '').trim();
    if (Object.hasOwn(changes, 'cargo')) payload.cargo = String(changes.cargo || '').trim();
    if (!ROLES.includes(payload.rol)) throw new Error('El rol seleccionado no es válido.');
    if (!['ACTIVO', 'INACTIVO'].includes(payload.estadoUsuario)) throw new Error('El estado seleccionado no es válido.');
    if (Object.hasOwn(payload, 'nombres') && (!payload.nombres || !payload.apellidos || !payload.cargo)) throw new Error('Nombres, apellidos y cargo son obligatorios.');
    if (id === this.session?.user?.uid && payload.estadoUsuario === 'INACTIVO') throw new Error('No puedes inactivar tu propia sesión.');
    if (id === this.session?.user?.uid && payload.rol !== normalizeUpper(this.session?.profile?.rol)) throw new Error('No puedes cambiar el rol de tu propia sesión.');
    if (this.mode === 'firebase') {
      payload.fechaActualizacion = this.firebase.serverTimestamp();
      await this.firebase.updateDoc(this.firebase.doc(this.db, 'usuarios', id), payload);
      if (id === this.session?.user?.uid) {
        const { fechaActualizacion, ...sessionChanges } = payload;
        this.session.profile = { ...this.session.profile, ...sessionChanges };
      }
      return;
    }
    const database = this.readLocalDatabase();
    const user = (database.usuarios || []).find(item => item.id === id || item.uid === id);
    if (!user) throw new Error('No se encontró el usuario.');
    Object.assign(user, payload);
    this.writeLocalDatabase(database);
    if (this.session?.profile?.uid === user.uid) this.session.profile = { ...this.session.profile, ...payload };
  }

  async updateAsset(asset, changes) {
    const code = normalizeUpper(asset.codigo);
    const positionCount = Number(changes.cantidadPosiciones);
    if (!Number.isInteger(positionCount) || positionCount < 0 || positionCount > 12) throw new Error('Las posiciones de llanta deben ser un número entero entre 0 y 12.');
    if ((changes.permiteTomaHuella || changes.permiteIntervencionLlanta) && positionCount < 1) throw new Error('Configura al menos una posición para habilitar los formularios de llantas.');
    const payload = {
      ubicacionActual: String(changes.ubicacionActual || '').trim().toUpperCase(),
      status: normalizeUpper(changes.status),
      intervalo: Number(changes.intervalo) || 0,
      cantidadPosiciones: positionCount,
      permitePreventivo: Boolean(changes.permitePreventivo),
      permiteCorrectivo: Boolean(changes.permiteCorrectivo),
      permiteTomaHuella: Boolean(changes.permiteTomaHuella),
      permiteIntervencionLlanta: Boolean(changes.permiteIntervencionLlanta),
      aplicaVulcanizacion: Boolean(changes.permiteTomaHuella || changes.permiteIntervencionLlanta),
      fechaActualizacion: nowIso()
    };
    if (this.mode === 'firebase') {
      payload.fechaActualizacion = this.firebase.serverTimestamp();
      let id = asset.id;
      if (!id) {
        const snapshot = await this.firebase.getDocs(this.firebase.query(
          this.firebase.collection(this.db, 'activos'),
          this.firebase.where('codigo', '==', code)
        ));
        id = snapshot.docs[0]?.id;
      }
      if (!id) throw new Error('No se encontró el activo en Firebase.');
      await this.firebase.updateDoc(this.firebase.doc(this.db, 'activos', id), payload);
    } else {
      const database = this.readLocalDatabase();
      database.assetOverrides ||= {};
      database.assetOverrides[code] = { ...(database.assetOverrides[code] || {}), ...payload };
      this.writeLocalDatabase(database);
    }
    const index = this.assets.findIndex(item => normalizeUpper(item.codigo) === code);
    if (index >= 0) this.assets[index] = { ...this.assets[index], ...payload };
  }

  exportDemoBackup() {
    return clone(this.readLocalDatabase());
  }
}

function fileToDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onload = () => resolve(reader.result);
    reader.onerror = () => reject(new Error('No se pudo leer la evidencia.'));
    reader.readAsDataURL(file);
  });
}
