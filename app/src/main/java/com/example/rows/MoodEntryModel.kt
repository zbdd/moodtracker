package com.example.rows

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.time.LocalDateTime

@IgnoreExtraProperties
class MoodEntryModel(
    val date: String? = "1987-11-06",
    val time: String? = "08:30",
    val mood: Mood? = Mood(),
    val feelings: MutableList<String> = ArrayList(),
    val activities: MutableList<String> = ArrayList(),
    key: String? = "mood_entry_key",
    var lastUpdated: String? = LocalDateTime.now().toString()
):RowEntryModel(),
    Serializable {

    override var key = key
    override var viewType: Int = MOOD_ENTRY_TYPE

    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "date" to date,
            "time" to time,
            "mood" to mood,
            "feelings" to feelings,
            "activities" to activities,
            "key" to key
        )
    }

    fun compare(moodEntry: MoodEntryModel): Boolean {
        var isTheSame = true

        if (!date.equals(moodEntry.date)) isTheSame = false
        if (!time.equals(moodEntry.time)) isTheSame = false
        if (mood != moodEntry.mood) isTheSame = false
        if (activities != moodEntry.activities) isTheSame = false
        if (feelings != moodEntry.feelings) isTheSame = false

        return isTheSame
    }
}
