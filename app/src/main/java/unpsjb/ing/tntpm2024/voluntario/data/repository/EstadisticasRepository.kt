package unpsjb.ing.tntpm2024.voluntario.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import unpsjb.ing.tntpm2024.voluntario.data.model.DevolucionNutricional
import unpsjb.ing.tntpm2024.voluntario.data.model.NutrientesTotales

class EstadisticasRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("encuestas")

    fun obtenerDevolucionNutricional(
        encuestaAsociadaId: String,
        onSuccess: (DevolucionNutricional) -> Unit,
        onError: (String) -> Unit
    ) {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    onError("No hay encuestas disponibles para procesar estadísticas.")
                    return
                }

                var misNutrientes = NutrientesTotales()
                var sumaKcal = 0.0
                var sumaCarbos = 0.0
                var sumaProteinas = 0.0
                var sumaColesterol = 0.0
                var sumaFibra = 0.0
                var totalEncuestasValidas = 0

                for (encuestaSnap in snapshot.children) {
                    val alimentosSnap = encuestaSnap.child("alimentos")

                    var kcalEncuesta = 0.0
                    var carbosEncuesta = 0.0
                    var proteinasEncuesta = 0.0
                    var colesterolEncuesta = 0.0
                    var fibraEncuesta = 0.0

                    for (item in alimentosSnap.children) {
                        // Multiplicador de frecuencia o porción si lo procesás, o valor base
                        val veces = item.child("veces").getValue(Any::class.java)?.toString()?.toDoubleOrNull() ?: 1.0

                        val kcal = item.child("kcal").getValue(Double::class.java) ?: 0.0
                        val carbos = item.child("carbohidratos").getValue(Double::class.java) ?: 0.0
                        val proteinas = item.child("proteinas").getValue(Double::class.java) ?: 0.0
                        val colesterol = item.child("colesterol").getValue(Double::class.java) ?: 0.0
                        val fibra = item.child("fibra").getValue(Double::class.java) ?: 0.0

                        kcalEncuesta += (kcal * veces)
                        carbosEncuesta += (carbos * veces)
                        proteinasEncuesta += (proteinas * veces)
                        colesterolEncuesta += (colesterol * veces)
                        fibraEncuesta += (fibra * veces)
                    }

                    // Sumamos a la muestra global
                    sumaKcal += kcalEncuesta
                    sumaCarbos += carbosEncuesta
                    sumaProteinas += proteinasEncuesta
                    sumaColesterol += colesterolEncuesta
                    sumaFibra += fibraEncuesta
                    totalEncuestasValidas++

                    // Identificamos la encuesta propia del voluntario
                    if (encuestaSnap.key == encuestaAsociadaId) {
                        misNutrientes = NutrientesTotales(
                            kcal = kcalEncuesta,
                            carbohidratos = carbosEncuesta,
                            proteinas = proteinasEncuesta,
                            colesterol = colesterolEncuesta,
                            fibra = fibraEncuesta
                        )
                    }
                }

                val cantidad = if (totalEncuestasValidas > 0) totalEncuestasValidas else 1
                val promedios = NutrientesTotales(
                    kcal = sumaKcal / cantidad,
                    carbohidratos = sumaCarbos / cantidad,
                    proteinas = sumaProteinas / cantidad,
                    colesterol = sumaColesterol / cantidad,
                    fibra = sumaFibra / cantidad
                )

                val devolucion = DevolucionNutricional(
                    totalVoluntarios = totalEncuestasValidas,
                    misNutrientes = misNutrientes,
                    promediosGenerales = promedios
                )

                onSuccess(devolucion)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }
}