package com.example.rows

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MoodEntryModel (
    val date: String? = null,
    val time: String? = null,
    val mood: String? = null,
    val activity: String? = null,
    val key: String? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "date" to date,
            "time" to time,
            "mood" to mood,
            "activity" to activity,
            "key" to key
        )
    }
}
