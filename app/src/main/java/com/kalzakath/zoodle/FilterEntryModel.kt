package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

class FilterEntryModel (
    var title: String = "",
    override var date: String = "1990-01-01",
    override var time: String = "09:09",
    override var key: String = "default_row_key"

        ): RowEntryModel {

    @Transient
    lateinit var viewHolder: RowViewHolder
    override var viewType: Int = 2

    override fun bindToViewHolder(holder: RecyclerView.ViewHolder) {
        viewHolder = holder as FilterViewHolder
        (viewHolder as FilterViewHolder).tvFilterTitle.text = title
    }

    override fun getViewHolder(): RecyclerView.ViewHolder {
        return viewHolder
    }

    override fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "date" to date,
            "time" to time,
            "key" to key
        )
    }
}