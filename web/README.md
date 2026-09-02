# MEV Mantenimiento Web 2.2

Versión web completa del proyecto de tesis **MEV Mantenimiento**, compatible con el mismo proyecto Firebase utilizado por la aplicación Android. También incorpora un modo demostración local para presentar el sistema aunque no exista conexión a Internet o no estén disponibles las credenciales.

## Inicio rápido en Windows

Para una revisión rápida sin instalar dependencias:

1. Haz doble clic en `INICIAR_WEB.bat`.
2. El navegador abrirá automáticamente `http://localhost:5500`.
3. Mantén abierta la ventana negra mientras utilizas la aplicación.

## Ejecutar desde Visual Studio Code

La compilación publicable requiere Node.js 22.13 o posterior. Abre esta carpeta en Visual Studio Code y ejecuta:

```powershell
pnpm install
pnpm dev
```

Después abre la dirección local que muestre Vite. Para generar el paquete de producción:

```powershell
pnpm build
pnpm preview
```

La versión publicada consulta directamente el mismo Firebase que utiliza Android. El botón **Actualizar datos** vuelve a cargar activos y registros sin cerrar la sesión.

## Comprobar el proyecto

Puedes hacer doble clic en `COMPROBAR_PROYECTO.bat` o ejecutar:

```powershell
npm.cmd run check
npm.cmd test
```

La comprobación valida la sintaxis, los módulos principales, los ocho roles, la interpretación por voz y el catálogo de 162 activos.

## Formas de acceso

### Firebase

Usa las mismas credenciales de la aplicación Android. El perfil debe existir en la colección `usuarios`, tener `estadoUsuario: "ACTIVO"` y uno de los ocho roles configurados.

### Modo demostración

En la pantalla de inicio selecciona un perfil y pulsa **Entrar en modo demostración**. Este modo:

- funciona sin credenciales;
- incluye registros de ejemplo realistas;
- guarda los cambios solamente en el navegador;
- no modifica Firebase;
- permite restablecer los datos desde el menú lateral.

Es la opción recomendada como respaldo durante la exposición.

## Funciones terminadas

- Inicio de sesión con Firebase Authentication y recuperación de contraseña.
- Modo demostración sin conexión con los ocho perfiles.
- Menú y permisos visibles según rol.
- Mantenimiento preventivo y correctivo.
- Toma general de huella con 2 a 12 posiciones según el activo.
- Detección de mediciones críticas iguales o menores a 6 mm.
- Intervención de llantas: cambio, rotación, reparación, montaje, desmontaje y baja.
- Guardado como BORRADOR y envío como ENVIADO.
- Edición de borradores y corrección de registros DEVUELTOS.
- Revisión, APROBACIÓN y DEVOLUCIÓN con motivo obligatorio.
- Historial unificado de mantenimiento y vulcanización.
- Evidencia fotográfica opcional con compresión automática.
- Dictado continuo por voz en español de Ecuador, con separación de varios campos, kilometraje y horómetro.
- Reportes con filtros por activo, proyecto, técnico, tipo, estado y fechas.
- Panel de indicadores y mediciones críticas.
- Exportación a Excel con cuatro hojas.
- Impresión y guardado en PDF desde el navegador.
- Matriz de 162 activos con búsqueda, filtros, detalle y configuración.
- Creación de credenciales y administración de datos, roles, estado y recuperación de contraseña de usuarios.
- Diseño adaptable a computadora, tableta y teléfono.
- Aplicación instalable y respaldo local para uso sin conexión.

## Roles

1. `TECNICO_MECANICO`
2. `VULCANIZADOR`
3. `SUPERVISOR_MANTENIMIENTO`
4. `ANALISTA_MANTENIMIENTO`
5. `ASISTENTE_PLANIFICACION`
6. `PLANIFICADOR`
7. `JEFE_OPERACIONES`
8. `GERENTE_GENERAL`

## Estructura principal

- `index.html`: documento principal.
- `src/main.js`: interfaz, navegación y flujos funcionales.
- `src/core.js`: roles, validaciones, cálculos y comandos de voz.
- `src/data-service.js`: Firebase y base local de demostración.
- `src/export.js`: exportación a Excel e impresión/PDF.
- `src/voice.js`: reconocimiento de voz del navegador.
- `src/styles.css`: diseño adaptable.
- `public/inventario_activos_firestore.json`: catálogo local de 162 activos.
- `public/service-worker.js`: disponibilidad local de los archivos esenciales.
- `worker/index.js`: entrada compatible con el alojamiento web.
- `vite.config.js` y `wrangler.jsonc`: compilación de producción.
- `servidor.js`: servidor local sin dependencias.
- `INICIAR_WEB.bat`: inicio automático.
- `COMPROBAR_PROYECTO.bat`: validación automática.
- `GUIA_RAPIDA_PRESENTACION.txt`: recorrido recomendado para la exposición.
- `firebase/`: reglas de referencia para Firestore y Storage.

## Reconocimiento de voz

El dictado funciona mejor en Google Chrome o Microsoft Edge y requiere permiso para usar el micrófono. Pulsa **Dictar datos**, indica varios campos seguidos y pulsa **Finalizar dictado**. Ejemplos:

- Mantenimiento: “Activo EVOLQ 87, servicio preventivo, kilometraje 84520, horómetro 6341, acción cambio de aceite y filtros, orden OT 184”.
- Huellas: “Activo EVOLQ 87, posición 1 huella 9.2, posición 2 huella 8.8, estado bueno”.
- Intervención: “Activo EVOLQ 87, cambio, posición 7, huella 5.8, marca Bridgestone, medida 12R22.5”.

Siempre revisa los campos antes de guardar o enviar.

## Firebase

La configuración pública del proyecto está incluida en `src/data-service.js`. Para el funcionamiento en línea deben cumplirse estas condiciones:

- Email/Password habilitado en Firebase Authentication.
- Dominio desde el que se ejecuta la versión publicada agregado a **Authorized domains**.
- Colecciones existentes: `usuarios`, `activos`, `registros_mantenimiento`, `tomas_huella` e `intervenciones_llanta`.
- Reglas de Firestore y Storage compatibles con los perfiles.
- Para crear usuarios desde la web, publica la versión incluida de `firebase/firestore.rules`.

Las reglas incluidas en `firebase/` sirven como plantilla y deben revisarse antes de publicarlas en un entorno real.

## Compatibilidad

- Windows 10 y Windows 11.
- Node.js 22.13 o superior para compilar y publicar.
- Google Chrome o Microsoft Edge actualizado.
- Resoluciones de escritorio y móviles.

---

Proyecto académico de Luis Barragán · Desarrollo de Software · 2026.
