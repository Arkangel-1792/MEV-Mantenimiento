# MEV Mantenimiento

![Identidad visual de MEV Mantenimiento](design/logo-mev-concept-v1.png)

Aplicación Android para registrar, revisar y analizar actividades de mantenimiento de **maquinarias, equipos y vehículos (MEV)**. El prototipo prioriza el trabajo en campo: permite completar formularios manualmente o mediante dictado, guardar borradores, enviar registros a revisión y conservar su trazabilidad en Firebase.

## Estado del proyecto

- Versión: `1.1.0-beta07` (`versionCode 7`)
- Rama de trabajo: `LuisB/usuarios-apk`
- Plataformas: Android 8.0 o superior (`minSdk 26`) y navegador web moderno
- Estado: versión beta demostrable para evaluación académica
- Pruebas automatizadas: Android 12 aprobadas y web 18 aprobadas
- APK de demostración: `entregables/MEV-Mantenimiento-1.1.0-beta07-debug.apk`
- Versión web: `web/` (`2.2.0`), conectada al mismo proyecto Firebase

## Funcionalidades implementadas

- Autenticación con correo y contraseña mediante Firebase Authentication.
- Menú dinámico según el rol registrado en el perfil del usuario.
- Catálogo consultable de activos con búsqueda por código, tipo, marca, modelo o ubicación.
- Registro de mantenimiento preventivo y correctivo.
- Toma general de huella con posiciones dinámicas por activo y límite de `0.1` a `26 mm`.
- Intervenciones de llantas: cambio, rotación, reparación, montaje, desmontaje y baja.
- Captura asistida por voz para completar uno o varios campos en una instrucción.
- Reconocimiento de códigos por tipo y secuencia, por ejemplo `tractor 9` → `MTRAC0009` y `mini 5` → `MMINI0005`.
- Borradores editables y corrección de registros devueltos.
- Flujo de estados `BORRADOR` → `ENVIADO` → `APROBADO` o `DEVUELTO`.
- Centro unificado de revisión para mantenimientos, huellas e intervenciones.
- Consulta separada de registros aprobados.
- Historial por usuario.
- Reportes con filtros por proyecto, activo, técnico, estado y fechas.
- Exportación de reportes a PDF y Excel.
- Identidad visual propia, icono de aplicación e iconos representativos por módulo.
- Administración de usuarios desde la APK para el rol `PLANIFICADOR`: creación, edición, activación, inactivación y recuperación de contraseña.

## Tecnologías

- Kotlin 2.2.10
- Jetpack Compose y Material 3
- Android Gradle Plugin 9.2.1
- Firebase Authentication
- Cloud Firestore
- Android `SpeechRecognizer`
- JUnit 4
- JavaScript modular, Vite y Cloudflare Workers para la versión web

## Arquitectura funcional

```text
Usuario
  │
  ├── Interfaz Jetpack Compose
  │     ├── Mantenimiento
  │     ├── Toma de huella
  │     ├── Intervención de llanta
  │     ├── Borradores, historial y revisión
  │     └── Reportes y exportación
  │
  ├── Parser contextual de voz
  │     └── VoiceCommandParser → campos del formulario
  │
  └── Capa de datos
        ├── Firebase Authentication
        └── Cloud Firestore
              ├── usuarios
              ├── activos
              ├── registros_mantenimiento
              ├── tomas_huella
              └── intervenciones_llanta
```

## Roles contemplados

- `TECNICO_MECANICO`: mantenimiento, borradores e historial.
- `VULCANIZADOR`: huellas, intervenciones, borradores e historial.
- `SUPERVISOR_MANTENIMIENTO`: revisión y consulta de aprobados.
- `ANALISTA_MANTENIMIENTO`: revisión, aprobados y reportes.
- `ASISTENTE_PLANIFICACION`: registro, vulcanización, revisión y reportes.
- `PLANIFICADOR`: acceso operativo amplio, reportes y matriz base.
- `JEFE_OPERACIONES`: reportes y consulta de matriz base.
- `GERENTE_GENERAL`: reportes y opciones gerenciales.

> La versión beta aplica la visibilidad de opciones desde el perfil recuperado de Firestore. La administración de usuarios está disponible para `PLANIFICADOR`; el panel gerencial especializado queda como trabajo futuro. La interfaz no sustituye la necesidad de reglas de seguridad de Firestore para una publicación productiva.

## Requisitos de desarrollo

1. Android Studio con JDK 11 o superior.
2. SDK de Android compatible con `compileSdk 37`.
3. Un proyecto de Firebase con Authentication y Cloud Firestore.
4. Archivo `google-services.json` correspondiente al proyecto Firebase dentro de `app/`.
5. Dispositivo o emulador Android con servicio de reconocimiento de voz.

## Configuración de Firebase

1. Habilitar el método de inicio de sesión **Correo/contraseña**.
2. Crear los perfiles de usuario en la colección `usuarios`, usando el UID de Authentication como ID del documento.
3. Incluir al menos: `nombres`, `apellidos`, `cargo`, `rol`, `estadoUsuario` y `email`.
4. Cargar el catálogo en la colección `activos`; el archivo base de ejemplo se encuentra en `app/src/main/assets/inventario_activos_firestore.json`.
5. Definir y probar reglas de seguridad antes de usar información real.

Ejemplo mínimo de perfil:

```json
{
  "nombres": "Usuario",
  "apellidos": "Demostración",
  "cargo": "Técnico mecánico",
  "rol": "TECNICO_MECANICO",
  "estadoUsuario": "ACTIVO",
  "email": "usuario@ejemplo.com"
}
```

## Compilación y pruebas

En Windows:

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
```

El APK generado por Gradle se ubica en `app/build/outputs/apk/debug/app-debug.apk`.

Las pruebas automatizadas verifican:

- reconocimiento de activos como tractor y minicargadora;
- interpretación de “mantenimiento realizado”;
- separación de varios campos en un solo dictado;
- aceptación de huellas hasta 26 mm y rechazo de valores superiores.

## Uso de la voz

1. Abrir un formulario compatible.
2. Tocar el botón flotante del micrófono.
3. Dictar una instrucción breve y esperar el mensaje de confirmación.
4. Revisar los campos antes de guardar o enviar.

Ejemplos:

```text
tractor 9 mantenimiento realizado cambio de bandas
orden de trabajo OT-904 número de pedido 923
activo mini 5 proyecto Vivantino
posición 3 18 milímetros
tipo de intervención rotación posición 4 técnico José Aguirre
guardar borrador
enviar registro
```

El guion en identificadores puede depender del texto devuelto por el servicio de reconocimiento. Siempre se debe confirmar visualmente la orden antes del envío.

## Estructura principal

```text
app/src/main/java/com/luis/mevmantenimiento/
├── data/          # Repositorios, filtros, importación y exportación
├── ui/screens/    # Pantallas Jetpack Compose
├── ui/theme/      # Paleta, tema y tipografía
├── voice/         # Reconocimiento y parser contextual
└── MainActivity.kt
```

## Documentación

- `documentacion/Manual_de_Usuario_MEV_Mantenimiento.pdf`
- `documentacion/Tesis_Final_MEV_Mantenimiento.pdf`
- `documentacion/GUIA_PRESENTACION_20_MIN.md`

Las versiones editables en Word se conservan en la misma carpeta.

## Limitaciones conocidas y trabajo futuro

- Completar el panel gerencial especializado.
- Implementar reglas de seguridad de Firestore por rol y validar la autorización también en backend.
- Incorporar cambio de correo y autoservicio avanzado de cuentas.
- Añadir adjuntos fotográficos mediante almacenamiento seguro.
- Ampliar pruebas instrumentadas, de conectividad, rendimiento y usabilidad con usuarios reales.
- Preparar una compilación `release` firmada; el APK incluido es de depuración para demostración.

## Autoría

Proyecto académico desarrollado por **Luis Alberto Barragan Villota**, carrera de Desarrollo de Software, Instituto Superior Tecnológico Rumiñahui, 2026.
