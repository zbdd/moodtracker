package com.example.rows

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.get
import androidx.recyclerview.widget.RecyclerView


class FeelingsRecycleViewAdaptor(context: Context, data: MutableList<String>, val onClick: (String) -> Unit, val onClickDelete: (String) -> Unit): RecyclerView.Adapter<FeelingsRecycleViewAdaptor.FeelingsViewHolder>() {
    class FeelingsViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvFeelingsRowName)
        val tvCancel: TextView = itemView.findViewById(R.id.tvFeelingsRowCancel)
    }

    private var mData: MutableList<String>? = null
    private var mInflater: LayoutInflater? = null
    private lateinit var mGroup: ViewGroup

    init {
        mInflater = LayoutInflater.from(context)
        mData = data
        sortList()
    }

    fun toggleDeleteButton() {
        for(row in mData!!.indices) {
            val item = mGroup[row].findViewById<TextView>(R.id.tvFeelingsRowCancel)
            if (item.visibility == View.INVISIBLE) item.visibility = View.VISIBLE
            else item.visibility = View.INVISIBLE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeelingsViewHolder {
        val view: View = mInflater?.inflate(R.layout.feelings_recycle_view_row, parent,false) ?: View(parent.context)
        mGroup = parent
        return FeelingsViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeelingsViewHolder, position: Int) {
        holder.tvName.text = mData?.get(position) ?: "Error"

        holder.tvName.setOnClickListener {
            onClick(holder.tvName.text.toString())
            mData?.removeAt(position)
            notifyItemRemoved(position)
            sortList()
        }

        holder.tvCancel.setOnClickListener {
            mData?.removeAt(position)
            onClickDelete(holder.tvName.text.toString())
            notifyItemRemoved(position)
            sortList()
        }
    }

    fun addItem(activity: String) {
        if (mData!!.contains(activity)) return

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