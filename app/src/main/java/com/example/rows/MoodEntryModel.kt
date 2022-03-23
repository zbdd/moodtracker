package com.example.rows

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class MoodEntryModel (
    val date: String? = null,
    val mood: String? = null,
    val activity: String? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "date" to date,
            "mood" to mood,
            "activity" to activity
        )
    }
}
