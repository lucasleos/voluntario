package unpsjb.ing.tntpm2024.voluntario.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import unpsjb.ing.tntpm2024.voluntario.data.model.DevolucionNutricional
import java.util.Locale

@Composable
fun DevolucionScreen(
    modifier: Modifier = Modifier,
    devolucion: DevolucionNutricional
) {
    val mis = devolucion.misNutrientes
    val prom = devolucion.promediosGenerales

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text(
                text = "¡Encuesta Completada!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Texto formal solicitado en el punto 1c
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = String.format(
                        Locale.getDefault(),
                        "“Gracias por sumarte a nuestro proyecto, ya somos %d personas voluntarias. El promedio de consumo de kcal totales es de %.1f kcal, el de carbohidratos es de %.1fg, el de proteínas es de %.1fg, el de colesterol es de %.1fmg y el de fibra es de %.1fg”.",
                        devolucion.totalVoluntarios,
                        prom.kcal,
                        prom.carbohidratos,
                        prom.proteinas,
                        prom.colesterol,
                        prom.fibra
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Tu Dieta vs. Promedio General",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Ítems de comparación
        item {
            NutrienteCard(
                nombre = "Kcal Totales",
                valorPropio = String.format(Locale.getDefault(), "%.1f kcal", mis.kcal),
                valorPromedio = String.format(Locale.getDefault(), "%.1f kcal", prom.kcal)
            )
        }
        item {
            NutrienteCard(
                nombre = "Carbohidratos",
                valorPropio = String.format(Locale.getDefault(), "%.1f g", mis.carbohidratos),
                valorPromedio = String.format(Locale.getDefault(), "%.1f g", prom.carbohidratos)
            )
        }
        item {
            NutrienteCard(
                nombre = "Proteínas",
                valorPropio = String.format(Locale.getDefault(), "%.1f g", mis.proteinas),
                valorPromedio = String.format(Locale.getDefault(), "%.1f g", prom.proteinas)
            )
        }
        item {
            NutrienteCard(
                nombre = "Colesterol",
                valorPropio = String.format(Locale.getDefault(), "%.1f mg", mis.colesterol),
                valorPromedio = String.format(Locale.getDefault(), "%.1f mg", prom.colesterol)
            )
        }
        item {
            NutrienteCard(
                nombre = "Fibras",
                valorPropio = String.format(Locale.getDefault(), "%.1f g", mis.fibra),
                valorPromedio = String.format(Locale.getDefault(), "%.1f g", prom.fibra)
            )
        }
    }
}

@Composable
fun NutrienteCard(
    nombre: String,
    valorPropio: String,
    valorPromedio: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tu consumo: $valorPropio",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Promedio muestra",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = valorPromedio,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}