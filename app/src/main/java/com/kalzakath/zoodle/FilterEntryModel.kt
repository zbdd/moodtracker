package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

class FilterEntryModel (
    var title: String = "",
    override var key: String = "default_row_key",
    override var viewType: Int = FILTER_ENTRY_TYPE
        ): RowEntryModel(key, viewType) {

    @Transient private lateinit var viewHolder: FilterViewHolder

    override fun bindToViewHolder(holder: RecyclerView.ViewHolder) {
        viewHolder = holder as FilterViewHolder
        viewHolder.tvFilterTitle.text = title
    }

    override fun update() {
        TODO("Not yet implemented")
    }
}