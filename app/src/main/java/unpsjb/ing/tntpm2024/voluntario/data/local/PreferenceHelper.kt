package unpsjb.ing.tntpm2024.voluntario.data.local

import android.content.Context
import java.util.UUID

object PreferenceHelper {
    private const val PREF_NAME = "voluntario_prefs"
    private const val KEY_VOLUNTARIO_ID = "key_voluntario_id"
    private const val KEY_TURNO_ID = "key_turno_id"

    fun getVoluntarioId(context: Context): String {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString(KEY_VOLUNTARIO_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString(KEY_VOLUNTARIO_ID, id).apply()
        }
        return id
    }

    fun saveTurnoId(context: Context, turnoId: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_TURNO_ID, turnoId)
            .apply()
    }

    fun getTurnoId(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TURNO_ID, null)
    }
}