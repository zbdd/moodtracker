package com.kalzakath.zoodle

import androidx.recyclerview.widget.RecyclerView

class FilterEntryModel (
    var title: String = "",
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

    override fun update() {
        TODO("Not yet implemented")
    }
}