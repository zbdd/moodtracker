package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

abstract class RowEntryModel (
    @Transient open var key: String,
    @Transient open var viewType: Int
) {
    companion object {
        const val MOOD_ENTRY_TYPE: Int = 0
        const val FILTER_ENTRY_TYPE: Int = 1
    }

    abstract fun bindToViewHolder(holder: RecyclerView.ViewHolder)

    abstract fun update()
}