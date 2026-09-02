package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.luis.mevmantenimiento.data.DatosUsuario
import com.luis.mevmantenimiento.data.UsuarioAdministrado
import com.luis.mevmantenimiento.data.UsuariosRepository
import com.luis.mevmantenimiento.data.UsuariosValidation

@Composable
fun UsuariosScreen(
    onVolver: () -> Unit
) {
    val context = LocalContext.current
    var usuarios by remember { mutableStateOf<List<UsuarioAdministrado>>(emptyList()) }
    var cargando by remember { mutableStateOf(false) }
    var procesando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var textoBusqueda by remember { mutableStateOf("") }
    var mostrarFormulario by remember { mutableStateOf(false) }
    var usuarioEdicion by remember { mutableStateOf<UsuarioAdministrado?>(null) }

    fun recargar(mensajeExito: String = "") {
        cargando = true
        if (mensajeExito.isBlank()) mensaje = ""
        UsuariosRepository.cargarUsuarios(
            onFinalizado = { datos ->
                usuarios = datos
                cargando = false
                if (mensajeExito.isNotBlank()) mensaje = mensajeExito
            },
            onError = { error ->
                cargando = false
                mensaje = error
            }
        )
    }

    LaunchedEffect(Unit) {
        recargar()
    }

    val filtrados = remember(usuarios, textoBusqueda) {
        val busqueda = textoBusqueda.trim().lowercase()
        if (busqueda.isBlank()) usuarios else usuarios.filter { usuario ->
            usuario.nombres.lowercase().contains(busqueda) ||
                    usuario.apellidos.lowercase().contains(busqueda) ||
                    usuario.email.lowercase().contains(busqueda) ||
                    usuario.cargo.lowercase().contains(busqueda) ||
                    usuario.rol.lowercase().contains(busqueda)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Text(
                text = "Usuarios y roles",
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Crea cuentas, asigna permisos y administra el acceso al sistema.",
                style = MaterialTheme.typography.bodyMedium
            )

            if (mensaje.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = mensaje,
                    color = if (
                        mensaje.startsWith("Usuario") ||
                        mensaje.startsWith("Se envió")
                    ) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = {
                    usuarioEdicion = null
                    mensaje = ""
                    mostrarFormulario = true
                },
                enabled = !procesando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear usuario")
            }

            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = textoBusqueda,
                onValueChange = { textoBusqueda = it },
                label = { Text("Buscar usuario") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mostrados: ${filtrados.size} de ${usuarios.size}",
                    style = MaterialTheme.typography.bodySmall
                )
                TextButton(
                    onClick = { recargar() },
                    enabled = !cargando && !procesando
                ) {
                    Text("Actualizar")
                }
            }
        }

        if (cargando) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (filtrados.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (usuarios.isEmpty()) {
                        "No existen usuarios registrados."
                    } else {
                        "No se encontraron usuarios con ese criterio."
                    }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtrados, key = { it.uid }) { usuario ->
                    UsuarioCard(
                        usuario = usuario,
                        procesando = procesando,
                        onEditar = {
                            usuarioEdicion = usuario
                            mensaje = ""
                            mostrarFormulario = true
                        },
                        onRecuperarPassword = {
                            procesando = true
                            mensaje = "Enviando recuperación de contraseña..."
                            UsuariosRepository.enviarRecuperacionPassword(
                                email = usuario.email,
                                onFinalizado = {
                                    procesando = false
                                    mensaje = "Se envió la recuperación a ${usuario.email}."
                                },
                                onError = { error ->
                                    procesando = false
                                    mensaje = error
                                }
                            )
                        }
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            enabled = !procesando,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
        ) {
            Text("Volver al menú principal")
        }
    }

    if (mostrarFormulario) {
        key(usuarioEdicion?.uid ?: "NUEVO_USUARIO") {
            FormularioUsuarioDialog(
                usuario = usuarioEdicion,
                procesando = procesando,
                mensajeOperacion = mensaje,
                onCancelar = { if (!procesando) mostrarFormulario = false },
                onGuardar = { datos ->
                    procesando = true
                    mensaje = if (usuarioEdicion == null) {
                        "Creando usuario..."
                    } else {
                        "Actualizando usuario..."
                    }
                    val editando = usuarioEdicion
                    if (editando == null) {
                        UsuariosRepository.crearUsuario(
                            context = context,
                            datosOriginales = datos,
                            onFinalizado = { creado ->
                                procesando = false
                                mostrarFormulario = false
                                recargar("Usuario ${creado.email} creado correctamente.")
                            },
                            onError = { error ->
                                procesando = false
                                mensaje = error
                            }
                        )
                    } else {
                        UsuariosRepository.actualizarUsuario(
                            uid = editando.uid,
                            datosOriginales = datos,
                            onFinalizado = {
                                procesando = false
                                mostrarFormulario = false
                                recargar("Usuario ${editando.email} actualizado correctamente.")
                            },
                            onError = { error ->
                                procesando = false
                                mensaje = error
                            }
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun UsuarioCard(
    usuario: UsuarioAdministrado,
    procesando: Boolean,
    onEditar: () -> Unit,
    onRecuperarPassword: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${usuario.nombres} ${usuario.apellidos}".trim(),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(usuario.email, style = MaterialTheme.typography.bodyMedium)
            Text(usuario.cargo, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                text = "${formatearRolUsuario(usuario.rol)} · ${usuario.estadoUsuario}",
                color = if (usuario.estadoUsuario == "ACTIVO") {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onEditar,
                enabled = !procesando,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Editar usuario")
            }
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedButton(
                onClick = onRecuperarPassword,
                enabled = !procesando && usuario.email.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar recuperación de contraseña")
            }
        }
    }
}

@Composable
private fun FormularioUsuarioDialog(
    usuario: UsuarioAdministrado?,
    procesando: Boolean,
    mensajeOperacion: String,
    onCancelar: () -> Unit,
    onGuardar: (DatosUsuario) -> Unit
) {
    var nombres by remember { mutableStateOf(usuario?.nombres.orEmpty()) }
    var apellidos by remember { mutableStateOf(usuario?.apellidos.orEmpty()) }
    var email by remember { mutableStateOf(usuario?.email.orEmpty()) }
    var cargo by remember { mutableStateOf(usuario?.cargo.orEmpty()) }
    var rol by remember { mutableStateOf(usuario?.rol ?: "GERENTE_GENERAL") }
    var estado by remember { mutableStateOf(usuario?.estadoUsuario ?: "ACTIVO") }
    var password by remember { mutableStateOf("") }
    var menuRolesVisible by remember { mutableStateOf(false) }
    var errorFormulario by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(if (usuario == null) "Crear usuario" else "Editar usuario")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                if (mensajeOperacion.isNotBlank()) {
                    Text(
                        text = mensajeOperacion,
                        color = if (procesando) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (errorFormulario.isNotBlank()) {
                    Text(
                        text = errorFormulario,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { nombres = it },
                    label = { Text("Nombres") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = apellidos,
                    onValueChange = { apellidos = it },
                    label = { Text("Apellidos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    enabled = usuario == null,
                    label = { Text("Correo electrónico") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = cargo,
                    onValueChange = { cargo = it },
                    label = { Text("Cargo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { menuRolesVisible = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rol: ${formatearRolUsuario(rol)}")
                    }
                    DropdownMenu(
                        expanded = menuRolesVisible,
                        onDismissRequest = { menuRolesVisible = false }
                    ) {
                        UsuariosValidation.roles.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(formatearRolUsuario(opcion)) },
                                onClick = {
                                    rol = opcion
                                    menuRolesVisible = false
                                }
                            )
                        }
                    }
                }
                Text("Estado de acceso", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = estado == "ACTIVO",
                        onClick = { estado = "ACTIVO" },
                        label = { Text("Activo") }
                    )
                    FilterChip(
                        selected = estado == "INACTIVO",
                        onClick = { estado = "INACTIVO" },
                        label = { Text("Inactivo") }
                    )
                }
                if (usuario == null) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña temporal") },
                        supportingText = { Text("Mínimo 6 caracteres") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "El correo no se modifica desde la APK. Puedes enviar una recuperación de contraseña desde la tarjeta del usuario.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val datos = DatosUsuario(
                        nombres = nombres,
                        apellidos = apellidos,
                        email = email,
                        cargo = cargo,
                        rol = rol,
                        estadoUsuario = estado,
                        passwordTemporal = password
                    )
                    val error = UsuariosValidation.validar(
                        datos = datos,
                        requierePassword = usuario == null
                    )
                    if (error == null) {
                        errorFormulario = ""
                        onGuardar(datos)
                    } else {
                        errorFormulario = error
                    }
                },
                enabled = !procesando
            ) {
                Text(if (procesando) "Guardando..." else "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar, enabled = !procesando) {
                Text("Cancelar")
            }
        }
    )
}

private fun formatearRolUsuario(rol: String): String = rol
    .lowercase()
    .replace("_", " ")
    .replaceFirstChar { it.uppercase() }
