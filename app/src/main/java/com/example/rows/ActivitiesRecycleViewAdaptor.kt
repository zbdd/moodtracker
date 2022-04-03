package com.example.rows

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ActivitiesRecycleViewAdaptor(context: Context, data: MutableList<String>, val onClick: (String) -> Unit): RecyclerView.Adapter<ActivitiesRecycleViewAdaptor.ViewHolder>() {
    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
    }

    private var mData: MutableList<String>? = null
    private var mInflater: LayoutInflater? = null

    init {
        mInflater = LayoutInflater.from(context)
        mData = data
        sortList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View = mInflater?.inflate(R.layout.activities_recycle_view_row, parent,false) ?: View(parent.context)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvName.text = mData?.get(position) ?: "Error"

        holder.tvName.setOnClickListener {
            onClick(holder.tvName.text.toString())
            mData?.removeAt(position)
            notifyItemRemoved(position)
            sortList()
        }
    }

    fun addItem(activity: String) {
        mData?.add(activity)
        notifyItemInserted(mData?.size?.minus(1) ?: 0)
        sortList()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortList() {
        mData?.sort()
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mData?.size ?: 0
    }
}