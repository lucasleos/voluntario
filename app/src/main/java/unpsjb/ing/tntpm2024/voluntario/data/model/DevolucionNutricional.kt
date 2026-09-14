package unpsjb.ing.tntpm2024.voluntario.data.model

data class NutrientesTotales(
    val kcal: Double = 0.0,
    val carbohidratos: Double = 0.0,
    val proteinas: Double = 0.0,
    val colesterol: Double = 0.0,
    val fibra: Double = 0.0
)

data class DevolucionNutricional(
    val totalVoluntarios: Int = 0,
    val misNutrientes: NutrientesTotales = NutrientesTotales(),
    val promediosGenerales: NutrientesTotales = NutrientesTotales()
)