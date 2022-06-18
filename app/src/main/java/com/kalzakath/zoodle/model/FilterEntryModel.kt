package com.kalzakath.zoodle.model

import androidx.recyclerview.widget.RecyclerView
import com.kalzakath.zoodle.FilterViewHolder
import com.kalzakath.zoodle.RowViewHolder
import com.kalzakath.zoodle.interfaces.RowEntryModel

data class FilterEntryModel (
    var title: String = "",
    override var date: String = "1990-01-01",
    override var time: String = "09:09",
    override var key: String = "default_row_key"

        ): RowEntryModel {

    @Transient
    lateinit var viewHolder: RowViewHolder
    override var viewType: Int = 2
}

fun FilterEntryModel.bindToViewHolder(holder: RecyclerView.ViewHolder) {
    val viewHolder = holder as FilterViewHolder
    viewHolder.tvFilterTitle.text = title
}

fun FilterEntryModel.toMap(): Map<String, Any?> {
    return mapOf(
        "title" to title,
        "date" to date,
        "time" to time,
        "key" to key
    )
}