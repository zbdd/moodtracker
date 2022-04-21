package com.kalzakath.zoodle

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MoodEntryViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val dateText: TextView = itemView.findViewById(R.id.tvMoodDate)
    val timeText: TextView = itemView.findViewById(R.id.tvMoodTime)
    val moodText: TextView = itemView.findViewById(R.id.tvMoodRating)
    val activityText: TextView = itemView.findViewById(R.id.etActivityText)
    val feelingsText: TextView = itemView.findViewById(R.id.tvMainRowFeelings)
}