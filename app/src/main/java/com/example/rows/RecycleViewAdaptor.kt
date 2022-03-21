package com.example.rows

import android.app.DatePickerDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class RecyclerViewAdaptor(private val moodList: List<MoodEntryModel>): RecyclerView.Adapter<RecyclerViewAdaptor.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.mood_entry_layout, parent, false);

        return ViewHolder(view)
    }

    fun updateDateText(calendar: Calendar, dateText: TextView) {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        dateText.text = date.format(calendar.time)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val MoodViewHolder = moodList[position]
        holder.dateText.text = MoodViewHolder.date
        holder.moodText.setText(MoodViewHolder.mood)
        holder.activityText.setText(MoodViewHolder.activity)

        val calendar: Calendar = Calendar.getInstance(TimeZone.getDefault())

        val calendarButton: ImageButton = holder.itemView.findViewById(R.id.calendarButton)

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                updateDateText(calendar, holder.dateText)
            }

        calendarButton.setOnClickListener(object: View.OnClickListener {
            override fun onClick(view: View) {
                DatePickerDialog(holder.itemView.context, dateSetListener,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).show()
            }
        })
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    class ViewHolder(ItemView: View): RecyclerView.ViewHolder(ItemView) {
        val dateText: TextView = itemView.findViewById(R.id.dateText)
        val moodText: EditText = itemView.findViewById(R.id.moodText)
        val activityText: EditText = itemView.findViewById(R.id.activityText)
    }
}