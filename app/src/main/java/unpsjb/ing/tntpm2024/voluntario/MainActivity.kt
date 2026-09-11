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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.database.FirebaseDatabase
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
                    // Leemos si ya existe un turno solicitado previamente
                    var turnoGuardadoId by remember {
                        mutableStateOf(PreferenceHelper.getTurnoId(this@MainActivity))
                    }

                    SolicitudTurnoScreen(
                        modifier = Modifier.padding(innerPadding),
                        turnoId = turnoGuardadoId,
                        onSolicitarTurno = {
                            solicitarTurnoEnFirebase { nuevoId ->
                                turnoGuardadoId = nuevoId
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
        val turnoId = nuevoTurnoRef.key

        if (turnoId == null) {
            Toast.makeText(this, "Error al generar identificador", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevoTurno = Turno(
            turnoId = turnoId,
            voluntarioId = voluntarioId,
            estado = "SOLICITADO"
        )

        nuevoTurnoRef.setValue(nuevoTurno)
            .addOnSuccessListener {
                PreferenceHelper.saveTurnoId(this, turnoId)
                Toast.makeText(this, "¡Solicitud enviada con éxito!", Toast.LENGTH_SHORT).show()
                onSuccess(turnoId)
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun SolicitudTurnoScreen(
    modifier: Modifier = Modifier,
    turnoId: String?,
    onSolicitarTurno: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (turnoId == null) {
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
        } else {
            Text(
                text = "¡Solicitud registrada!",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Identificador de solicitud:\n$turnoId",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Estado: Esperando asignación de fecha y lugar por parte del encuestador...",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}