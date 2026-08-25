package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.luis.mevmantenimiento.data.UsuarioResumen
import com.luis.mevmantenimiento.data.UsuariosRepository

@Composable
fun UsuariosScreen(
    onVolver: () -> Unit
) {
    val context = LocalContext.current

    var usuarios by remember {
        mutableStateOf<List<UsuarioResumen>>(emptyList())
    }
    var cargando by remember { mutableStateOf(true) }
    var guardando by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var usuarioEnEdicion by remember {
        mutableStateOf<UsuarioResumen?>(null)
    }
    var mostrarFormulario by remember { mutableStateOf(false) }

    fun cargarUsuarios() {
        cargando = true
        UsuariosRepository.cargar(
            onFinalizado = {
                usuarios = it
                cargando = false
            },
            onError = {
                mensaje = it
                cargando = false
            }
        )
    }

    LaunchedEffect(Unit) {
        cargarUsuarios()
    }

    if (mostrarFormulario) {
        FormularioUsuario(
            usuario = usuarioEnEdicion,
            guardando = guardando,
            mensaje = mensaje,
            onGuardar = { usuario, passwordTemporal ->
                guardando = true
                mensaje = "Guardando usuario..."

                val finalizado = {
                    guardando = false
                    mensaje = "Usuario guardado correctamente."
                    mostrarFormulario = false
                    usuarioEnEdicion = null
                    cargarUsuarios()
                }

                val error: (String) -> Unit = {
                    guardando = false
                    mensaje = it
                }

                if (usuario.uid.isBlank()) {
                    UsuariosRepository.crear(
                        context = context,
                        usuario = usuario,
                        passwordTemporal = passwordTemporal,
                        onFinalizado = finalizado,
                        onError = error
                    )
                } else {
                    UsuariosRepository.actualizar(
                        usuario = usuario,
                        onFinalizado = finalizado,
                        onError = error
                    )
                }
            },
            onVolver = {
                mostrarFormulario = false
                usuarioEnEdicion = null
                mensaje = ""
            }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Administración de usuarios",
                style = MaterialTheme.typography.headlineSmall
            )
            Text("Usuarios registrados: ${usuarios.size}")
            Button(
                onClick = {
                    usuarioEnEdicion = null
                    mensaje = ""
                    mostrarFormulario = true
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Crear usuario")
            }
            if (mensaje.isNotBlank()) {
                Text(mensaje)
            }
        }

        if (cargando) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (usuarios.isEmpty()) {
                    item { Text("No existen perfiles registrados.") }
                }

                items(usuarios, key = { it.uid }) { usuario ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "${usuario.nombres} ${usuario.apellidos}".trim(),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(usuario.email)
                            Text(usuario.cargo.ifBlank { "Sin cargo" })
                            Text(formatearRolUsuario(usuario.rol))
                            Text(usuario.estadoUsuario)
                            Button(
                                onClick = {
                                    usuarioEnEdicion = usuario
                                    mensaje = ""
                                    mostrarFormulario = true
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Editar usuario")
                            }
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text("Volver al menú")
        }
    }
}

@Composable
private fun FormularioUsuario(
    usuario: UsuarioResumen?,
    guardando: Boolean,
    mensaje: String,
    onGuardar: (UsuarioResumen, String) -> Unit,
    onVolver: () -> Unit
) {
    val esNuevo = usuario == null
    var nombres by remember(usuario?.uid) {
        mutableStateOf(usuario?.nombres.orEmpty())
    }
    var apellidos by remember(usuario?.uid) {
        mutableStateOf(usuario?.apellidos.orEmpty())
    }
    var email by remember(usuario?.uid) {
        mutableStateOf(usuario?.email.orEmpty())
    }
    var cargo by remember(usuario?.uid) {
        mutableStateOf(usuario?.cargo.orEmpty())
    }
    var rol by remember(usuario?.uid) {
        mutableStateOf(
            usuario?.rol?.ifBlank { "TECNICO_MECANICO" }
                ?: "TECNICO_MECANICO"
        )
    }
    var estado by remember(usuario?.uid) {
        mutableStateOf(usuario?.estadoUsuario ?: "ACTIVO")
    }
    var password by remember(usuario?.uid) { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                if (esNuevo) "Crear usuario" else "Editar usuario",
                style = MaterialTheme.typography.headlineSmall
            )
        }
        item {
            OutlinedTextField(
                nombres,
                { nombres = it },
                Modifier.fillMaxWidth(),
                label = { Text("Nombres *") },
                enabled = !guardando
            )
        }
        item {
            OutlinedTextField(
                apellidos,
                { apellidos = it },
                Modifier.fillMaxWidth(),
                label = { Text("Apellidos") },
                enabled = !guardando
            )
        }
        item {
            OutlinedTextField(
                email,
                { email = it },
                Modifier.fillMaxWidth(),
                label = { Text("Correo *") },
                enabled = esNuevo && !guardando,
                singleLine = true
            )
        }
        if (esNuevo) {
            item {
                OutlinedTextField(
                    password,
                    { password = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("Contraseña temporal *") },
                    enabled = !guardando,
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        }
        item {
            OutlinedTextField(
                cargo,
                { cargo = it },
                Modifier.fillMaxWidth(),
                label = { Text("Cargo") },
                enabled = !guardando
            )
        }

        item {
            Text("Rol", style = MaterialTheme.typography.titleMedium)
        }
        items(UsuariosRepository.rolesDisponibles) { rolDisponible ->
            FilterChip(
                selected = rol == rolDisponible,
                onClick = { rol = rolDisponible },
                label = { Text(formatearRolUsuario(rolDisponible)) },
                enabled = !guardando
            )
        }

        item {
            Text("Estado", style = MaterialTheme.typography.titleMedium)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterChip(
                    selected = estado == "ACTIVO",
                    onClick = { estado = "ACTIVO" },
                    label = { Text("Activo") },
                    enabled = !guardando
                )
                FilterChip(
                    selected = estado == "INACTIVO",
                    onClick = { estado = "INACTIVO" },
                    label = { Text("Inactivo") },
                    enabled = !guardando
                )
            }
        }

        if (mensaje.isNotBlank()) {
            item { Text(mensaje) }
        }

        item {
            Button(
                onClick = {
                    onGuardar(
                        UsuarioResumen(
                            uid = usuario?.uid.orEmpty(),
                            nombres = nombres,
                            apellidos = apellidos,
                            email = email,
                            cargo = cargo,
                            rol = rol,
                            estadoUsuario = estado
                        ),
                        password
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando &&
                        nombres.isNotBlank() &&
                        email.isNotBlank() &&
                        (!esNuevo || password.length >= 6)
            ) {
                Text(if (guardando) "Guardando..." else "Guardar usuario")
            }
        }
        item {
            OutlinedButton(
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth(),
                enabled = !guardando
            ) {
                Text("Volver")
            }
        }
    }
}

private fun formatearRolUsuario(
    rol: String
): String {
    return rol
        .lowercase()
        .replace('_', ' ')
        .replaceFirstChar { it.uppercase() }
}
