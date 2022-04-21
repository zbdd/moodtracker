package com.kalzakath.zoodle

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FilterViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
    val tvFilterTitle: TextView = itemView.findViewById(R.id.tvFilterTitle)
}