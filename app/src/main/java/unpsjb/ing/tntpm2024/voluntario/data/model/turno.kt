package unpsjb.ing.tntpm2024.voluntario.model

data class AsignacionTurno(
    val fecha: String = "",
    val hora: String = "",
    val lugar: String = "",
    val indicaciones: String = ""
)

data class Turno(
    var turnoId: String = "",
    val voluntarioId: String = "",
    var fcmToken: String = "",
    var estado: String = "SOLICITADO", // "SOLICITADO", "ASIGNADO", "COMPLETADO"
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val asignacion: AsignacionTurno? = null,
    val encuestaAsociadaId: String? = null
)