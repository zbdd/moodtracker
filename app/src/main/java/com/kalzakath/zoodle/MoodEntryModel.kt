package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.memberProperties

@IgnoreExtraProperties
class MoodEntryModel(
    override var date: String = "1987-11-06",
    override var time: String = "08:30",
    val mood: Mood? = Mood(),
    var feelings: MutableList<String> = ArrayList(),
    var activities: MutableList<String> = ArrayList(),
    override var key: String = "local_" + UUID.randomUUID().toString(),
    var lastUpdated: String = LocalDateTime.now().toString()
):RowEntryModel,
    Serializable {

    @Transient private lateinit var viewHolder: RecyclerView.ViewHolder
    override fun getViewHolder(): RecyclerView.ViewHolder {
        return viewHolder
    }

    override var viewType: Int = 1

    @Exclude
    override fun toMap(): Map<String, Any?> {
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

    fun update(moodEntry: MoodEntryModel) {
        for (prop in MoodEntryModel::class.memberProperties) {
            val updateValues =
                moodEntry::class.java.declaredFields.find { it.name == prop.name }
            if (updateValues != null)
                if (updateValues.name != "viewHolder")
                    if (prop is KMutableProperty<*>) {
                        println("INFO INFO: ${updateValues.name}")
                        prop.setter.call(this, updateValues.get(moodEntry))
                    }
        }
    }

    fun compare(moodEntry: MoodEntryModel): Boolean {
        var isTheSame = true

        if (date != moodEntry.date) isTheSame = false
        if (time != moodEntry.time) isTheSame = false
        if (mood?.value != moodEntry.mood?.value) isTheSame = false
        if (activities != moodEntry.activities) isTheSame = false
        if (feelings != moodEntry.feelings) isTheSame = false

        return isTheSame
    }

    override fun bindToViewHolder(holder: RecyclerView.ViewHolder) {
        val mViewHolder = holder as MoodViewHolder
        mViewHolder.dateText.text = date
        mViewHolder.timeText.text = time
        val moodHelper = MoodValueHelper()

        if (mood != null) {
            if (Settings.moodMode == Settings.MoodModes.FACES)

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

        if (::viewHolder.isInitialized) {
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
    }
}
