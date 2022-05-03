package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

interface RowEntryModel {
    var key: String
    var viewType: Int

    abstract fun bindToViewHolder(holder: RecyclerView.ViewHolder)

    abstract fun update()
}