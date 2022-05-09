package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView
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
    var lastUpdated: String? = LocalDateTime.now().toString()
):RowEntryModel,
    Serializable {

    @Transient private lateinit var viewHolder: RecyclerView.ViewHolder
    override fun getViewHolder(): RecyclerView.ViewHolder {
        return viewHolder
    }

    override var viewType: Int = 1

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
        if (mood?.value != moodEntry.mood?.value) isTheSame = false
        if (activities != moodEntry.activities || activities.isEmpty()) isTheSame = false
        if (feelings != moodEntry.feelings || activities.isEmpty()) isTheSame = false

        return isTheSame
    }

    override fun bindToViewHolder(holder: RecyclerView.ViewHolder) {
        val mViewHolder = holder as MoodViewHolder
        mViewHolder.dateText.text = date
        mViewHolder.timeText.text = time
        val moodHelper = MoodValueHelper()

        if (mood != null) {
            if (mood.moodMode == Mood.MOOD_MODE_FACES)

                mViewHolder.moodText.text = mViewHolder.itemView.resources.getString(
                    moodHelper.getEmoji(
                        mood.toFaces(
                            moodHelper.getSanitisedNumber(mood.value!!.toInt(), 5).toString()
                        )
                    )
                )
            else mViewHolder.moodText.text = mood.value
            mViewHolder.activityText.text = when (activities.toString()) {
                "[]" -> "Click to add an activity"
                else -> activities.toString().removeSurrounding(
                    "[",
                    "]"
                )
            }
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

    fun applyDrawable() {
        if (mood == null) return
        if (viewHolder != null) viewHolder = getViewHolder()

        val mViewHolder = viewHolder as MoodViewHolder

        when {
            mood.value
            !!.toInt() > 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_high)
            mood.value
            !!.toInt() < 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_low)
            mood.value
            !!.toInt() == 3 -> mViewHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour)
        }
    }

    override fun update(settings: Settings) {
        mood!!.moodMode = settings.moodMode
    }
}
