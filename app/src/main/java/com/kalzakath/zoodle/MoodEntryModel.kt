package com.kalzakath.zoodle

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
    override var key: String = "mood_entry_key",
    var lastUpdated: String? = LocalDateTime.now().toString(),
    override var viewType: Int = MOOD_ENTRY_TYPE
):RowEntryModel(key, viewType),
    Serializable {

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
        else if (!time.equals(moodEntry.time)) isTheSame = false
        else if (mood?.value != moodEntry.mood?.value) isTheSame = false
        else if (activities != moodEntry.activities) isTheSame = false
        else if (feelings != moodEntry.feelings) isTheSame = false

        return isTheSame
    }
}
