# Reglas de Firebase

Estas reglas son una plantilla de referencia para el proyecto `mev-mantenimiento`.

Antes de publicarlas:

1. Verifica que todos los usuarios tengan un documento en `usuarios/{uid}`.
2. Confirma que `estadoUsuario` sea `ACTIVO` y que `rol` coincida con uno de los ocho perfiles.
3. Prueba las reglas en Firebase Emulator o en el simulador de reglas.
4. Publica únicamente cuando hayas confirmado que las consultas de la aplicación móvil y web son compatibles.

Las reglas no permiten eliminar registros operativos ni usuarios desde el cliente. El perfil `PLANIFICADOR` puede crear perfiles, actualizar datos, roles y estados. La versión web crea la credencial en una sesión secundaria de Firebase Authentication para no cerrar la sesión del planificador.

La creación de usuarios desde la aplicación web requiere publicar esta versión de `firestore.rules`. La contraseña temporal solo se envía a Firebase Authentication y no se almacena en Firestore.
