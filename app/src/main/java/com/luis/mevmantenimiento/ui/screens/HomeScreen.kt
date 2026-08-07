package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.luis.mevmantenimiento.PerfilUsuario

data class OpcionMenu(
    val titulo: String,
    val descripcion: String
)

@Composable
fun MenuPrincipalScreen(
    perfil: PerfilUsuario,
    onSeleccionarOpcion: (String) -> Unit,
    onCerrarSesion: () -> Unit
) {
    val opciones = obtenerOpcionesPorRol(perfil.rol)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Bienvenido, ${perfil.nombres}",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = perfil.cargo,
                style = MaterialTheme.typography.bodyLarge
            )

            Text(
                text = "Rol: ${formatearRol(perfil.rol)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 20.dp,
                end = 20.dp,
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(opciones) { opcion ->
                OpcionMenuCard(
                    opcion = opcion,
                    onClick = {
                        onSeleccionarOpcion(opcion.titulo)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun OpcionMenuCard(
    opcion: OpcionMenu,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = opcion.titulo,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = opcion.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ingresar")
            }
        }
    }
}

private fun obtenerOpcionesPorRol(
    rol: String
): List<OpcionMenu> {
    return when (rol) {

        "TECNICO_MECANICO" -> listOf(
            OpcionMenu(
                titulo = "Nuevo mantenimiento",
                descripcion = "Registrar un mantenimiento preventivo o correctivo."
            ),
            OpcionMenu(
                titulo = "Mis borradores",
                descripcion = "Consultar, editar y enviar registros pendientes."
            ),
            OpcionMenu(
                titulo = "Mi historial",
                descripcion = "Consultar los trabajos registrados anteriormente."
            )
        )

        "VULCANIZADOR" -> listOf(
            OpcionMenu(
                titulo = "Toma general de huella",
                descripcion = "Registrar la medición general de las llantas de un activo."
            ),
            OpcionMenu(
                titulo = "Borradores de huella",
                descripcion = "Continuar y enviar tomas de huella guardadas."
            ),
            OpcionMenu(
                titulo = "Historial de huellas",
                descripcion = "Consultar tomas enviadas, aprobadas o devueltas."
            ),
            OpcionMenu(
                titulo = "Intervención de llanta",
                descripcion = "Registrar cambios, rotaciones, reparaciones o bajas."
            ),
            OpcionMenu(
                titulo = "Mis borradores",
                descripcion = "Continuar registros pendientes de envío."
            ),
            OpcionMenu(
                titulo = "Mi historial",
                descripcion = "Consultar las actividades de vulcanización registradas."
            )
        )

        "SUPERVISOR_MANTENIMIENTO" -> opcionesSupervisor()

        "ANALISTA_MANTENIMIENTO" -> opcionesSupervisor() + listOf(
            OpcionMenu(
                titulo = "Reportes",
                descripcion = "Consultar y analizar información operativa."
            )
        )

        "ASISTENTE_PLANIFICACION" -> listOf(
            OpcionMenu(
                titulo = "Nuevo mantenimiento",
                descripcion = "Registrar actividades preventivas o correctivas."
            ),
            OpcionMenu(
                titulo = "Mis borradores",
                descripcion = "Consultar, editar y enviar registros pendientes."
            ),
            OpcionMenu(
                titulo = "Vulcanización",
                descripcion = "Registrar tomas de huella e intervenciones."
            ),
            OpcionMenu(
                titulo = "Revisión de registros",
                descripcion = "Revisar información enviada por el personal."
            ),
            OpcionMenu(
                titulo = "Reportes",
                descripcion = "Consultar reportes operativos."
            )
        )

        "PLANIFICADOR" -> listOf(
            OpcionMenu(
                titulo = "Nuevo mantenimiento",
                descripcion = "Registrar mantenimientos preventivos o correctivos."
            ),
            OpcionMenu(
                titulo = "Mis borradores",
                descripcion = "Consultar, editar y enviar registros pendientes."
            ),
            OpcionMenu(
                titulo = "Toma general de huella",
                descripcion = "Registrar la condición general de las llantas."
            ),
            OpcionMenu(
                titulo = "Revisión de huellas",
                descripcion = "Aprobar o devolver tomas de huella enviadas."
            ),
            OpcionMenu(
                titulo = "Intervención de llanta",
                descripcion = "Registrar cambios, reparaciones, rotaciones o bajas."
            ),
            OpcionMenu(
                titulo = "Mi historial",
                descripcion = "Consultar los registros de mantenimiento que he enviado."
            ),
            OpcionMenu(
                titulo = "Revisión de registros",
                descripcion = "Aprobar, devolver o corregir registros enviados."
            ),
            OpcionMenu(
                titulo = "Reportes",
                descripcion = "Consultar y generar informes de mantenimiento."
            ),
            OpcionMenu(
                titulo = "Matriz base",
                descripcion = "Administrar activos, proyectos y configuraciones."
            ),
            OpcionMenu(
                titulo = "Usuarios",
                descripcion = "Administrar usuarios, estados y roles."
            )
        )

        "JEFE_OPERACIONES" -> listOf(
            OpcionMenu(
                titulo = "Reportes",
                descripcion = "Consultar reportes e indicadores generales."
            ),
            OpcionMenu(
                titulo = "Matriz base",
                descripcion = "Consultar y actualizar la configuración de activos."
            )
        )

        "GERENTE_GENERAL" -> listOf(
            OpcionMenu(
                titulo = "Panel gerencial",
                descripcion = "Consultar indicadores y resultados consolidados."
            ),
            OpcionMenu(
                titulo = "Reportes",
                descripcion = "Consultar informes generales por proyecto."
            ),
            OpcionMenu(
                titulo = "Matriz base",
                descripcion = "Consultar y actualizar la configuración de activos."
            )
        )

        else -> emptyList()
    }
}

private fun opcionesSupervisor(): List<OpcionMenu> {
    return listOf(
        OpcionMenu(
            titulo = "Nuevo mantenimiento",
            descripcion = "Registrar actividades preventivas o correctivas."
        ),
        OpcionMenu(
            titulo = "Vulcanización",
            descripcion = "Registrar tomas de huella e intervenciones."
        ),
        OpcionMenu(
            titulo = "Revisión de registros",
            descripcion = "Visualizar registros por técnico o vulcanizador."
        )
    )
}

private fun formatearRol(
    rol: String
): String {
    return rol
        .lowercase()
        .replace("_", " ")
        .replaceFirstChar { letra ->
            letra.uppercase()
        }
}
