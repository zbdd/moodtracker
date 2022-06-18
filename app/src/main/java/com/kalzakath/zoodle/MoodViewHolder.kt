package com.kalzakath.zoodle

import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import java.text.SimpleDateFormat
import java.util.*

class MoodViewHolder(itemView: View) : RowViewHolder(itemView)  {
    val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
    val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
    val moodText: TextView= itemView.findViewById(R.id.tvMoodRating)
    val activityText: TextView = itemView.findViewById(R.id.etActivityText)
    val feelingsText: TextView = itemView.findViewById(R.id.tvMainRowFeelings)
    val body: ConstraintLayout = itemView.findViewById(R.id.cMoodEntry)
}

fun MoodViewHolder.updateDateTimeText(calendar: Calendar) {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    dateText.text = dateFormat.format(calendar.time)

    val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
    timeText.text = timeFormat.format(calendar.time)
}

