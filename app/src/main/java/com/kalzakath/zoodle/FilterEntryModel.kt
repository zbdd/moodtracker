package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

class FilterEntryModel (
    var title: String = "",
    override var key: String = "default_row_key"
        ): RowEntryModel {

    @Transient lateinit var viewHolder: FilterViewHolder
    override var viewType: Int = 2

    override fun bindToViewHolder(holder: RecyclerView.ViewHolder) {
        viewHolder = holder as FilterViewHolder
        viewHolder.tvFilterTitle.text = title
    }

    override fun update() {
        TODO("Not yet implemented")
    }
}