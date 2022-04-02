package com.example.rows

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable

@IgnoreExtraProperties
data class MoodEntryModel (
    val date: String? = null,
    val time: String? = null,
    val mood: String? = null,
    val activities: MutableList<String> = ArrayList<String>(),
    val key: String? = null
): Serializable {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "date" to date,
            "time" to time,
            "mood" to mood,
            "activities" to activities,
            "key" to key
        )
    }

    fun compare(moodEntry: MoodEntryModel): Boolean {
        var isTheSame = true

        if (!date.equals(moodEntry.date)) isTheSame = false
        if (!time.equals(moodEntry.time)) isTheSame = false
        if (!mood.equals(moodEntry.mood)) isTheSame = false
        if (activities != moodEntry.activities) isTheSame = false

        return isTheSame
    }
}
