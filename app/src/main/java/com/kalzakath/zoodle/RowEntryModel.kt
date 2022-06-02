package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

interface RowEntryModel {
    var date: String
    var time: String
    var key: String
    var viewType: Int

    fun bindToViewHolder(holder: RecyclerView.ViewHolder)

    fun getViewHolder(): RecyclerView.ViewHolder
}