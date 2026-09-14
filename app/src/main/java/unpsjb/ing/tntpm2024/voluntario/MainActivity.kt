package unpsjb.ing.tntpm2024.voluntario

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.database.*
import unpsjb.ing.tntpm2024.voluntario.data.local.PreferenceHelper
import unpsjb.ing.tntpm2024.voluntario.data.model.DevolucionNutricional
import unpsjb.ing.tntpm2024.voluntario.data.repository.EstadisticasRepository
import unpsjb.ing.tntpm2024.voluntario.model.Turno
import unpsjb.ing.tntpm2024.voluntario.ui.components.DevolucionScreen
import unpsjb.ing.tntpm2024.voluntario.ui.theme.VoluntarioTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.text.style.TextAlign

class MainActivity : ComponentActivity() {

    private val estadisticasRepo = EstadisticasRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            VoluntarioTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    var turnoActual by remember { mutableStateOf<Turno?>(null) }
                    var devolucionData by remember { mutableStateOf<DevolucionNutricional?>(null) }
                    var cargandoDevolucion by remember { mutableStateOf(false) }

                    var turnoIdGuardado by remember {
                        mutableStateOf(PreferenceHelper.getTurnoId(this@MainActivity))
                    }

                    // Listener reactivo del estado del turno
                    DisposableEffect(turnoIdGuardado) {
                        val id = turnoIdGuardado
                        if (id != null) {
                            val turnoRef = FirebaseDatabase.getInstance().getReference("turnos").child(id)
                            val listener = object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val turno = snapshot.getValue(Turno::class.java)
                                    turnoActual = turno

                                    // Si el turno está completado y tiene encuesta vinculada, pedimos la devolución
                                    if (turno?.estado == "COMPLETADO" && !turno.encuestaAsociadaId.isNullOrEmpty() && devolucionData == null) {
                                        cargandoDevolucion = true
                                        estadisticasRepo.obtenerDevolucionNutricional(
                                            encuestaAsociadaId = turno.encuestaAsociadaId,
                                            onSuccess = { data ->
                                                devolucionData = data
                                                cargandoDevolucion = false
                                            },
                                            onError = {
                                                cargandoDevolucion = false
                                            }
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {}
                            }
                            turnoRef.addValueEventListener(listener)
                            onDispose { turnoRef.removeEventListener(listener) }
                        } else {
                            onDispose {}
                        }
                    }

                    // Renderizado condicional
                    if (turnoActual?.estado == "COMPLETADO") {
                        if (cargandoDevolucion || devolucionData == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text("Calculando tu balance nutricional y los promedios globales...")
                                }
                            }
                        } else {
                            DevolucionScreen(
                                modifier = Modifier.padding(innerPadding),
                                devolucion = devolucionData!!
                            )
                        }
                    } else {
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

            // Caso 4: Estado ASIGNADO
            turno.estado == "ASIGNADO" && turno.asignacion != null -> {
                Text(
                    text = "¡Turno Asignado!",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
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
                        Text(
                            text = "Fecha: ${turno.asignacion.fecha}",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hora: ${turno.asignacion.hora}",
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Lugar / Dirección:")
                        Text(
                            text = turno.asignacion.lugar,
                            style = MaterialTheme.typography.bodyMedium
                        )

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
        }
    }
}