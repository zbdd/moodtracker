package com.kalzakath.zoodle

import android.view.View
import android.widget.TextView

class MoodViewHolder(itemView: View) : RowViewHolder(itemView)  {
    val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
    val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
    val moodText: TextView= itemView.findViewById(R.id.tvMoodRating)
    val activityText: TextView = itemView.findViewById(R.id.etActivityText)
    val feelingsText: TextView = itemView.findViewById(R.id.tvMainRowFeelings)
}