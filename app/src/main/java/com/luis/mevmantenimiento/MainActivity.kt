package com.luis.mevmantenimiento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.luis.mevmantenimiento.ui.theme.MEVMantenimientoTheme
import com.luis.mevmantenimiento.ui.screens.MenuPrincipalScreen
import com.luis.mevmantenimiento.ui.screens.MatrizBaseScreen
import com.luis.mevmantenimiento.ui.screens.ActivoResumen
import com.luis.mevmantenimiento.ui.screens.ActivosScreen
import com.luis.mevmantenimiento.data.ImportadorActivos
import com.luis.mevmantenimiento.ui.screens.DetalleActivoScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            MEVMantenimientoTheme {

                var perfilUsuario by remember {
                    mutableStateOf<PerfilUsuario?>(null)
                }

                var pantallaActual by remember {
                    mutableStateOf("MENU")
                }

                var activos by remember {
                    mutableStateOf<List<ActivoResumen>>(emptyList())
                }
                var activoSeleccionado by remember {
                    mutableStateOf<ActivoResumen?>(null)
                }
                var importandoActivos by remember {
                    mutableStateOf(false)
                }

                var progresoImportacion by remember {
                    mutableStateOf("")
                }

                var mensajeImportacion by remember {
                    mutableStateOf("")
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (perfilUsuario == null) {
                            LoginScreen(
                                onLoginSuccess = { perfil ->
                                    perfilUsuario = perfil
                                    pantallaActual = "MENU"
                                }
                            )
                        } else {
                            when (pantallaActual) {
                                 "DETALLE_ACTIVO" -> {
                                    activoSeleccionado?.let { activo ->
                                        DetalleActivoScreen(
                                            activo = activo,
                                            onEditar = {
                                                // Después crearemos el formulario de edición.
                                            },
                                            onVolver = {
                                                pantallaActual = "ACTIVOS"
                                            }
                                        )
                                    }
                                }
                                "ACTIVOS" -> {
                                    ActivosScreen(
                                        activos = activos,
                                        onSeleccionarActivo = { activo ->
                                            activoSeleccionado = activo
                                            pantallaActual = "DETALLE_ACTIVO"
                                        },
                                        onAgregarActivo = {
                                            // Después crearemos el formulario para agregar activos.
                                        },
                                        onVolver = {
                                            pantallaActual = "MATRIZ_BASE"
                                        }
                                    )
                                }
                                "MATRIZ_BASE" -> {
                                    MatrizBaseScreen(
                                        importandoActivos = importandoActivos,
                                        progresoImportacion = progresoImportacion,
                                        mensajeImportacion = mensajeImportacion,
                                        onSeleccionarModulo = { modulo ->

                                            when (modulo) {

                                                "ACTIVOS" -> {
                                                    mensajeImportacion = "Cargando activos..."

                                                    ImportadorActivos.cargarActivos(
                                                        onFinalizado = { datos ->

                                                            activos = datos.map { activo ->
                                                                ActivoResumen(
                                                                    codigo = activo["codigo"]?.toString().orEmpty(),
                                                                    subtipo = activo["subtipo"]?.toString().orEmpty(),
                                                                    tipo = activo["tipo"]?.toString().orEmpty(),
                                                                    marca = activo["marca"]?.toString().orEmpty(),
                                                                    modelo = activo["modelo"]?.toString().orEmpty(),
                                                                    indicador = activo["indicador"]?.toString().orEmpty(),
                                                                    horometro = (activo["horometro"] as? Number)?.toDouble(),
                                                                    kilometraje = (activo["kilometraje"] as? Number)?.toDouble(),
                                                                    ubicacionActual = activo["ubicacionActual"]?.toString().orEmpty(),
                                                                    status = activo["status"]?.toString().orEmpty()
                                                                )
                                                            }.sortedBy { activo ->
                                                                activo.codigo
                                                            }

                                                            mensajeImportacion = ""
                                                            pantallaActual = "ACTIVOS"
                                                        },

                                                        onError = { mensaje ->
                                                            mensajeImportacion = mensaje
                                                        }
                                                    )
                                                }

                                                "IMPORTAR_EXCEL" -> {
                                                    if (!importandoActivos) {

                                                        importandoActivos = true
                                                        progresoImportacion = "Preparando importación..."
                                                        mensajeImportacion = ""

                                                        ImportadorActivos.importar(
                                                            context = this@MainActivity,

                                                            onProgreso = { actual, total ->
                                                                progresoImportacion =
                                                                    "Importando activos: $actual de $total"
                                                            },

                                                            onFinalizado = { cantidad ->
                                                                importandoActivos = false
                                                                progresoImportacion = ""
                                                                mensajeImportacion =
                                                                    "Importación finalizada: $cantidad activos cargados."
                                                            },

                                                            onError = { mensaje ->
                                                                importandoActivos = false
                                                                progresoImportacion = ""
                                                                mensajeImportacion = mensaje
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        },

                                        onVolver = {
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }

                                else -> {
                                    // Menú principal existente
                                    MenuPrincipalScreen(
                                        perfil = perfilUsuario!!,
                                        onSeleccionarOpcion = { opcion ->
                                            if (opcion == "Matriz base") {
                                                pantallaActual = "MATRIZ_BASE"
                                            }
                                        },
                                        onCerrarSesion = {
                                            FirebaseAuth.getInstance().signOut()
                                            perfilUsuario = null
                                            pantallaActual = "MENU"
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class PerfilUsuario(
    val uid: String,
    val nombres: String,
    val apellidos: String,
    val email: String,
    val cargo: String,
    val rol: String,
    val estadoUsuario: String
)

@Composable
fun LoginScreen(
    onLoginSuccess: (PerfilUsuario) -> Unit
) {
    var email by remember {
        mutableStateOf("")
    }

    var password by remember {
        mutableStateOf("")
    }

    var mostrarPassword by remember {
        mutableStateOf(false)
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var mensaje by remember {
        mutableStateOf("")
    }

    val focusManager = LocalFocusManager.current
    val auth = remember { FirebaseAuth.getInstance() }
    val firestore = remember { FirebaseFirestore.getInstance() }

    fun iniciarSesion() {
        if (email.isBlank() || password.isBlank()) {
            mensaje = "Ingresa el correo y la contraseña."
            return
        }

        cargando = true
        mensaje = ""

        auth.signInWithEmailAndPassword(
            email.trim(),
            password
        ).addOnCompleteListener { tarea ->

            if (!tarea.isSuccessful) {
                cargando = false
                mensaje = "Correo o contraseña incorrectos."
                return@addOnCompleteListener
            }

            val uid = auth.currentUser?.uid

            if (uid == null) {
                cargando = false
                mensaje = "No fue posible identificar al usuario."
                auth.signOut()
                return@addOnCompleteListener
            }

            firestore.collection("usuarios")
                .document(uid)
                .get()
                .addOnSuccessListener { documento ->

                    if (!documento.exists()) {
                        cargando = false
                        mensaje = "El usuario no tiene un perfil registrado."
                        auth.signOut()
                        return@addOnSuccessListener
                    }

                    val estadoUsuario =
                        documento.getString("estadoUsuario").orEmpty()

                    if (estadoUsuario != "ACTIVO") {
                        cargando = false
                        mensaje = "El usuario se encuentra inactivo."
                        auth.signOut()
                        return@addOnSuccessListener
                    }

                    val perfil = PerfilUsuario(
                        uid = uid,
                        nombres = documento.getString("nombres").orEmpty(),
                        apellidos = documento.getString("apellidos").orEmpty(),
                        email = documento.getString("email").orEmpty(),
                        cargo = documento.getString("cargo").orEmpty(),
                        rol = documento.getString("rol").orEmpty(),
                        estadoUsuario = estadoUsuario
                    )

                    cargando = false
                    onLoginSuccess(perfil)
                }
                .addOnFailureListener {
                    cargando = false
                    mensaje = "No se pudo consultar el perfil del usuario."
                    auth.signOut()
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MEV Mantenimiento",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Gestión de mantenimiento de flota",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Correo electrónico")
            },
            singleLine = true,
            enabled = !cargando,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Contraseña")
            },
            singleLine = true,
            enabled = !cargando,
            visualTransformation = if (mostrarPassword) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = {
                        mostrarPassword = !mostrarPassword
                    }
                ) {
                    Text(
                        text = if (mostrarPassword) {
                            "Ocultar"
                        } else {
                            "Mostrar"
                        }
                    )
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                    iniciarSesion()
                }
            )
        )

        if (mensaje.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                iniciarSesion()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.height(22.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Iniciar sesión")
            }
        }
    }
}

@Composable
fun HomeScreen(
    perfil: PerfilUsuario,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenido, ${perfil.nombres}",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = perfil.cargo,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Rol: ${perfil.rol}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLogout
        ) {
            Text("Cerrar sesión")
        }
    }
}