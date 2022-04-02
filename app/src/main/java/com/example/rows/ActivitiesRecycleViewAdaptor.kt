package com.example.rows

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ActivitiesRecycleViewAdaptor(context: Context, data: List<String>): RecyclerView.Adapter<ActivitiesRecycleViewAdaptor.ViewHolder>() {
    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }

    private var mData: List<String>? = null
    private var mInflater: LayoutInflater? = null

    init {
        mInflater = LayoutInflater.from(context)
        mData = data
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater?.inflate(R.layout.activities_recycle_view_row, parent,false) ?: View(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = mData?.get(position) ?: "Error"
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }
}