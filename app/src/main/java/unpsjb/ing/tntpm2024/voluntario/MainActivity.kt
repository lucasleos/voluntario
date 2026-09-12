package unpsjb.ing.tntpm2024.voluntario

import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import unpsjb.ing.tntpm2024.voluntario.data.local.PreferenceHelper
import unpsjb.ing.tntpm2024.voluntario.model.Turno
import unpsjb.ing.tntpm2024.voluntario.ui.theme.VoluntarioTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VoluntarioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var turnoActual by remember { mutableStateOf<Turno?>(null) }
                    var turnoIdGuardado by remember {
                        mutableStateOf(PreferenceHelper.getTurnoId(this@MainActivity))
                    }

                    // Escuchador en tiempo real del turno
                    DisposableEffect(turnoIdGuardado) {
                        val id = turnoIdGuardado
                        if (id != null) {
                            val turnoRef = FirebaseDatabase.getInstance().getReference("turnos").child(id)
                            val listener = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val turno = snapshot.getValue(Turno::class.java)
                                    turnoActual = turno
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            }
                            turnoRef.addValueEventListener(listener)
                            onDispose { turnoRef.removeEventListener(listener) }
                        } else {
                            onDispose {}
                        }
                    }

                    VoluntarioScreenContent(
                        modifier = Modifier.padding(innerPadding),
                        turno = turnoActual,
                        turnoIdGuardado = turnoIdGuardado,
                        onSolicitarTurno = {
                            solicitarTurnoEnFirebase { nuevoId ->
                                turnoIdGuardado = nuevoId
                            }
                        }
                    )
                }
            }
        }
    }

    private fun solicitarTurnoEnFirebase(onSuccess: (String) -> Unit) {
        val voluntarioId = PreferenceHelper.getVoluntarioId(this)
        val dbRef = FirebaseDatabase.getInstance().getReference("turnos")

        val nuevoTurnoRef = dbRef.push()
        val turnoId = nuevoTurnoRef.key ?: return

        val nuevoTurno = Turno(
            turnoId = turnoId,
            voluntarioId = voluntarioId,
            estado = "SOLICITADO"
        )

        nuevoTurnoRef.setValue(nuevoTurno)
            .addOnSuccessListener {
                PreferenceHelper.saveTurnoId(this, turnoId)
                Toast.makeText(this, "¡Solicitud registrada!", Toast.LENGTH_SHORT).show()
                onSuccess(turnoId)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun VoluntarioScreenContent(
    modifier: Modifier = Modifier,
    turno: Turno?,
    turnoIdGuardado: String?,
    onSolicitarTurno: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when {
            // Caso 1: Aún no solicitó turno
            turnoIdGuardado == null -> {
                Text(
                    text = "Bienvenido/a al Proyecto de Hábitos Alimenticios",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Presioná el botón para postularte como voluntario/a y solicitar un turno presencial.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onSolicitarTurno) {
                    Text(text = "Quiero participar / Solicitar turno")
                }
            }

            // Caso 2: Ya solicitó pero todavía está cargando desde Firebase
            turno == null -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Cargando información del turno...")
            }

            // Caso 3: Estado SOLICITADO
            turno.estado == "SOLICITADO" -> {
                Text(
                    text = "¡Solicitud en espera!",
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Identificador de turno:\n${turno.turnoId}",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Tu solicitud fue recibida. Pronto el equipo te asignará fecha, hora y lugar.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Caso 4: Estado ASIGNADO (Punto 1b completado)
            turno.estado == "ASIGNADO" && turno.asignacion != null -> {
                Text(
                    text = "¡Turno Asignado!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Presentate en el siguiente día y horario para la encuesta:",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(20.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Fecha: ${turno.asignacion.fecha}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Hora: ${turno.asignacion.hora}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Lugar / Dirección:")
                        Text(text = turno.asignacion.lugar, style = MaterialTheme.typography.bodyMedium)

                        if (turno.asignacion.indicaciones.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "Indicaciones:")
                            Text(
                                text = turno.asignacion.indicaciones,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Caso 5: Estado COMPLETADO (para el punto 1c)
            turno.estado == "COMPLETADO" -> {
                Text(
                    text = "Encuesta Completada",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Preparando tu devolución nutricional y los promedios globales...",
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}