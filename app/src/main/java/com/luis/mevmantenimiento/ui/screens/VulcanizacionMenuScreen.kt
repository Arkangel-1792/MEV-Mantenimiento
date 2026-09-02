package com.luis.mevmantenimiento.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VulcanizacionMenuScreen(
    mensaje: String,
    onTomaHuella: () -> Unit,
    onIntervencion: () -> Unit,
    onVolver: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vulcanización",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Selecciona el registro que deseas realizar.",
            modifier = Modifier.padding(top = 8.dp, bottom = 20.dp)
        )

        if (mensaje.isNotBlank()) {
            Text(
                text = mensaje,
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

        Button(
            onClick = onTomaHuella,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Toma general de huella")
        }

        Button(
            onClick = onIntervencion,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        ) {
            Text("Intervención de llanta")
        }

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
        ) {
            Text("Volver al menú")
        }
    }
}
