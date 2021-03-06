package com.kalzakath.zoodle.model

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.IgnoreExtraProperties
import com.kalzakath.zoodle.*
import com.kalzakath.zoodle.interfaces.RowEntryModel
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

@IgnoreExtraProperties
data class MoodEntryModel(
    override var date: String = "1987-11-06",
    override var time: String = "08:30",
    var mood: Int = 3,
    var feelings: MutableList<String> = ArrayList(),
    var activities: MutableList<String> = ArrayList(),
    override var key: String = "local_" + UUID.randomUUID().toString(),
    var lastUpdated: String = LocalDateTime.now().toString(),
    var sleep: Int = 0,
    var medication: Boolean = false
): RowEntryModel,
    Serializable {

    var isVisible = true
    override var viewType: Int = 1

    @Transient var viewHolder: RecyclerView.ViewHolder? = null
}

fun MoodEntryModel.updateDateTime(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)

    date = dateFormat.format(calendar.time)
    time = timeFormat.format(calendar.time)
}

fun MoodEntryModel.compare(moodEntry: MoodEntryModel): Boolean {
    var isTheSame = true

    if (date != moodEntry.date) isTheSame = false
    if (time != moodEntry.time) isTheSame = false
    if (mood != moodEntry.mood) isTheSame = false
    if (activities != moodEntry.activities) isTheSame = false
    if (feelings != moodEntry.feelings) isTheSame = false

    return isTheSame
}

fun MoodEntryModel.hideRow(mViewHolder: MoodViewHolder) {
    if (isVisible) mViewHolder.body.maxHeight = 200
    else mViewHolder.body.maxHeight = 1
}

fun MoodEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "date" to date,
        "time" to time,
        "mood" to mood,
        "feelings" to feelings,
        "activities" to activities,
        "key" to key,
        "lastUpdated" to lastUpdated
    )
}
fun MoodEntryModel.update(moodEntry: MoodEntryModel) {
    lastUpdated = LocalDateTime.now().toString()

    date = moodEntry.date
    time = moodEntry.time
    mood = moodEntry.mood
    activities = moodEntry.activities
    feelings = moodEntry.feelings
    sleep = moodEntry.sleep
    medication = moodEntry.medication

}

fun MoodEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder) {
    val mViewHolder = holder as MoodViewHolder
    mViewHolder.dateText.text = date
    mViewHolder.timeText.text = time
    val moodHelper = MoodValueHelper()

    hideRow(mViewHolder)

        if (Settings.moodMode == Settings.MoodModes.FACES)

            mViewHolder.moodText.text = mViewHolder.itemView.resources.getString(
                moodHelper.getEmoji(
                    toFaces(
                        moodHelper.getSanitisedNumber(mood, 5)
                    )
                )
            )
        else mViewHolder.moodText.text = mood.toString()
        mViewHolder.activityText.text = when (activities.toString()) {
            "[]" -> "Click to add an activity"
            else -> activities.toString().removeSurrounding(
                "[",
                "]"
            )
        }

    mViewHolder.feelingsText.text = when (feelings.toString()) {
        "[]" -> "Click to add feelings"
        else -> feelings.toString().removeSurrounding(
            "[",
            "]"
        )
    }

    viewHolder = holder
    applyDrawable()
}

fun MoodEntryModel.applyDrawable() {
    if (viewHolder != null) {
        val mViewHolder = viewHolder as MoodViewHolder

        when {
            mood > 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_high)
            mood < 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_low)
            mood == 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour)
        }
    }
}