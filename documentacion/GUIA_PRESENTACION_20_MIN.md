# Guía de presentación final - MEV Mantenimiento

Duración objetivo: **18 minutos de exposición + 2 minutos de margen**.

## 1. Apertura y problema (2 minutos)

Mensaje central:

> MEV Mantenimiento centraliza el registro y seguimiento de mantenimientos de maquinarias, equipos y vehículos. Su aporte diferencial es la captura asistida por voz para reducir escritura en campo sin eliminar la revisión humana.

Explicar brevemente:

- Los registros dispersos dificultan la trazabilidad.
- El personal de campo necesita capturar datos con rapidez.
- La flota es heterogénea: cada activo puede usar kilometraje, horómetro y distinta cantidad de posiciones de llanta.

## 2. Objetivo y alcance (2 minutos)

Objetivo general:

> Desarrollar una aplicación Android que permita registrar, revisar, consultar y reportar actividades de mantenimiento y vulcanización, incorporando reconocimiento de voz como mecanismo de captura asistida.

Alcance demostrable:

- acceso por usuario y menú según rol;
- mantenimiento preventivo y correctivo;
- toma de huella e intervención de llanta;
- borradores, envío, aprobación y devolución;
- historial, aprobados, filtros y exportación;
- parser contextual de voz y validaciones.

## 3. Solución técnica (3 minutos)

- Kotlin + Jetpack Compose + Material 3.
- Firebase Authentication para la sesión.
- Cloud Firestore para activos, usuarios y registros.
- `SpeechRecognizer` de Android para convertir voz en texto.
- Parser propio para transformar frases en comandos de formulario.
- Repositorios separados para mantenimiento, vulcanización y reportes.

Frase útil:

> El reconocimiento de voz no guarda información por sí solo: propone valores, muestra confirmación y permite al usuario revisar antes de enviar.

## 4. Demostración organizada (7 minutos)

Preparación previa:

- Instalar `MEV-Mantenimiento-1.1.0-beta06-debug.apk`.
- Confirmar conexión a Internet y permiso de micrófono.
- Tener un usuario técnico y uno supervisor listos.
- Evitar depender de datos recién creados: mantener al menos un registro enviado de respaldo.

Secuencia recomendada:

1. Iniciar sesión y mostrar que el menú corresponde al rol.
2. Abrir **Nuevo mantenimiento**.
3. Dictar: `tractor 9 mantenimiento realizado cambio de bandas`.
4. Mostrar el activo `MTRAC0009`, revisar los campos y guardar borrador.
5. Abrir **Mis borradores**, editar el registro y enviarlo.
6. Ingresar como supervisor o planificador y abrir **Revisión de registros**.
7. Aprobar el registro o devolver otro con motivo.
8. Mostrar **Registros aprobados**.
9. Abrir **Toma general de huella** y explicar el límite máximo de 26 mm.
10. Mostrar **Reportes**, aplicar un filtro y enseñar las opciones PDF/Excel.

Plan B si falla la voz:

- explicar que depende del servicio de reconocimiento del dispositivo;
- completar manualmente los mismos campos;
- enseñar un registro previamente creado y continuar el flujo de revisión.

## 5. Pruebas y resultados (2 minutos)

- Compilación y pruebas unitarias ejecutadas correctamente.
- 9 pruebas aprobadas y 0 fallos.
- Casos automatizados para parser de activos, mantenimiento realizado, dictado múltiple y huellas de 0.1 a 26 mm.
- Pruebas en dispositivo físico sobre formularios, borradores, revisión y registros aprobados.

No afirmar ahorro porcentual ni impacto productivo medido: todavía no se realizó una prueba controlada con operación real.

## 6. Conclusiones y cierre (2 minutos)

- El prototipo demuestra que es viable centralizar mantenimiento y vulcanización en una aplicación móvil.
- La voz funciona mejor como captura asistida y verificable que como automatización sin control.
- El flujo de estados mejora la trazabilidad de correcciones y aprobaciones.
- La configuración por activo permite trabajar con una flota heterogénea.

Trabajo futuro:

- reglas de seguridad por rol en Firestore;
- administración completa de usuarios;
- adjuntos fotográficos;
- pruebas con usuarios reales y medición de tiempos;
- versión `release` firmada.

## Preguntas probables

### ¿Por qué Firebase?

Porque permitió integrar autenticación y una base documental en la nube con el SDK Android, reduciendo infraestructura para el prototipo. Para producción se deben reforzar reglas, monitoreo y respaldo.

### ¿Cómo funciona el parser?

Normaliza el texto, detecta etiquetas o alias, extrae valores y devuelve comandos tipados. Por ejemplo, `mini 5` se transforma en `MMINI0005`. El formulario aplica el resultado y el usuario lo confirma.

### ¿Qué ocurre si la voz se equivoca?

El sistema muestra el texto y el resultado interpretado. El usuario puede corregir manualmente o volver a dictar antes de guardar o enviar.

### ¿Cómo se evita una huella imposible?

La interfaz y el repositorio validan las medidas. La versión actual acepta de 0.1 a 26 mm y rechaza valores superiores.

### ¿La aplicación está lista para producción?

Es una beta funcional para demostración académica. Antes de producción requiere reglas de seguridad por rol, pruebas con usuarios reales, monitoreo, respaldo y firma de distribución.

### ¿Qué aportó Scrum?

Permitió corregir el producto por iteraciones. Los hallazgos en activos, parser, huellas y borradores regresaron al backlog y se verificaron en incrementos posteriores.

## Lista final antes de presentar

- [ ] Teléfono cargado y sin modo ahorro extremo.
- [ ] Internet y reconocimiento de voz comprobados.
- [ ] APK beta06 instalado.
- [ ] Dos usuarios de demostración disponibles.
- [ ] Datos de respaldo ya cargados.
- [ ] PDF de la tesis y manual copiados al teléfono o nube.
- [ ] Cronómetro configurado para 18 minutos.
- [ ] No abrir módulos incompletos: Usuarios o Panel gerencial.
