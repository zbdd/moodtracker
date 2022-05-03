package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.ceil

class RecyclerViewAdaptor(
    val onSwiped: (MoodEntryModel, ArrayList<MoodEntryModel>) -> Unit,
    val onListUpdated: (ArrayList<MoodEntryModel>) -> Unit,
    val onMoodValueClicked: (MoodEntryModel) -> Unit,
    val onStartActivitiesActivity: (MoodEntryModel) -> Unit,
    val startFeelingsActivity: (MoodEntryModel) -> Unit,
    var settings: Settings = Settings()):
    Adapter<ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor {

    private var moodList: ArrayList<RowEntryModel> = ArrayList()
    private var sortBy = "date"
    lateinit var viewHolder: ViewHolder

    fun getMoodList(): ArrayList<MoodEntryModel> {
        val moodArray = ArrayList<MoodEntryModel>()

        for(item in moodList) {
            when (item.viewType) {
                RowEntryModel.MOOD_ENTRY_TYPE -> moodArray.add(item as MoodEntryModel)
            }
        }
        return moodArray
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        viewHolder = when (viewType) {
            RowEntryModel.FILTER_ENTRY_TYPE -> FilterViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.filter_entry_layout, parent, false))
            else -> MoodEntryViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.mood_entry_layout, parent, false))
        }
        return viewHolder
    }

    fun updateListConfig(config: Settings) {
        settings = config

        for(i in moodList.indices) {
            if (moodList[i].viewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                val mood = moodList[i] as MoodEntryModel
                mood.mood!!.moodMode = settings.moodMode
                notifyItemChanged(i)
            }
        }
    }

    fun updateList(data: ArrayList<MoodEntryModel> = ArrayList(0)) {
        val removeList: MutableList<MoodEntryModel> = ArrayList()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)

        if (data.isEmpty()) return

        for (moodEntry in data) {
            for (i in moodList.indices) {
                if (moodList[i].viewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                    val moodListItem = moodList[i] as MoodEntryModel
                    if (moodListItem.key.equals(moodEntry.key)) {
                        if (LocalDate.parse(moodListItem.lastUpdated?.substring(0,19), format) < LocalDate.parse(
                                moodEntry.lastUpdated?.substring(0,19),
                                format
                            )
                        ) {
                            moodList[i] = moodEntry
                            removeList.add(moodEntry)
                            notifyItemChanged(i)
                        } else removeList.add(moodEntry)
                    } else if (moodListItem.compare(moodEntry)) {
                        if (LocalDate.parse(moodListItem.lastUpdated?.substring(0,19), format) < LocalDate.parse(
                                moodEntry.lastUpdated?.substring(0,19),
                                format
                            )
                        ) {
                            moodList[i] = moodEntry
                            removeList.add(moodEntry)
                            notifyItemChanged(i)
                        } else removeList.add(moodEntry)
                    }
                }
            }
        }
        for (item in removeList) {
            data.remove(item)
        }
        for (moodEntry in data) {
            moodList.add(moodEntry)
        }

        data.clear()
        sortList()

        val listToSave = ArrayList<MoodEntryModel>()
        for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
        onListUpdated(listToSave)
    }

    fun updateMoodEntry(mood: MoodEntryModel) {
        var position = -1

        for(i in moodList.indices) {
            if (moodList[i].viewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                val row = moodList[i] as MoodEntryModel
                if (row.key == mood.key) { position = i; break }
            }
        }
        if (position != -1) {
            notifyItemChanged(position)
            moodList[position] = mood
            sortList()

            val listToSave = ArrayList<MoodEntryModel>()
            for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
            onListUpdated(listToSave)

            if (viewHolder.itemViewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                val mHolder = viewHolder as MoodEntryViewHolder
                val moodE = moodList[position] as MoodEntryModel
                if (moodE.mood!!.moodMode == Mood.MOOD_MODE_NUMBERS) {
                    when {
                        mHolder.moodText.text.toString()
                            .toInt() > 3 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_high)
                        mHolder.moodText.text.toString()
                            .toInt() < 3 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_low)
                        mHolder.moodText.text.toString()
                            .toInt() == 3 -> mHolder.moodText.setBackgroundResource(0)
                    }
                } else mHolder.moodText.setBackgroundResource(0)
            }
        }
    }

    private fun sortList() {
        val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH)
        val comparator = compareBy({ mood: MoodEntryModel -> LocalDate.parse(mood.date, dateFormatter) }, { mood: MoodEntryModel -> LocalTime.parse(mood.time, timeFormatter) }).reversed()

        val moods: ArrayList<MoodEntryModel> = ArrayList()

        for(i in moodList.indices) {
            if (moodList[i].viewType == RowEntryModel.MOOD_ENTRY_TYPE) moods.add(moodList[i] as MoodEntryModel)
        }

        val sorted = when (sortBy) {
            "date" -> { moods.sortedWith (comparator) }
            //TODO "mood" -> moods.sortedByDescending { moodEntry -> moodEntry.mood }
            else -> moods
        }

        moods.clear()
        moodList.clear()
        moodList.addAll(sorted)

        for (x in moodList.indices) {
            if (sorted.contains(moodList[x])) {
                if (sorted.indexOf(moodList[x]) != x) {
                    notifyItemChanged(x)
                }
            }
        }

        addFilterView("Today")
        addFilterView("Last Week")
        addFilterView("Last Month")
        addFilterView("Last Year")
        addFilterView("Years Ago")
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addFilterView(title: String) {
        val moods: ArrayList<MoodEntryModel> = ArrayList()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        val validKeys: ArrayList<String> = ArrayList()
        validKeys.add("")

        for(i in moodList.indices) {
            when (moodList[i].viewType) {
                RowEntryModel.MOOD_ENTRY_TYPE -> { moods.add(moodList[i] as MoodEntryModel) }
                RowEntryModel.FILTER_ENTRY_TYPE -> {
                    val filterRow = moodList[i] as FilterEntryModel
                    moods.add (MoodEntryModel(
                    "2222-01-01",
                    "00:01",
                        Mood("5"),
                        ArrayList(),
                        ArrayList(),
                        filterRow.title
                ))
                    validKeys.add(filterRow.title)
                }
            }
        }

        if (moods.size == 0) return

        var maxDate: LocalDate = LocalDate.now()
        val minDate: LocalDate
        var pos: Int = -1
        var posLast = -1

        when (title) {
            "Today" -> {
                pos = moods.indexOfFirst { LocalDate.parse(it.date!!, format) == maxDate }
                posLast = moods.indexOfLast { LocalDate.parse(it.date!!, format) == maxDate }
            }
            "Last Week" -> {
                maxDate = LocalDate.parse("${LocalDate.now().minusWeeks(1)}", format)
                minDate = LocalDate.parse("${LocalDate.now().minusWeeks(2)}", format)
                pos = moods.indexOfFirst { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }
                posLast = moods.indexOfLast { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }
            }
            "Last Month" -> {
                var date: String = LocalDate.now().minusMonths(0).format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01"
                maxDate = LocalDate.parse(date, format)

                date = LocalDate.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-" + LocalDate.now().minusMonths(1).lengthOfMonth()
                minDate = LocalDate.parse(date, format)
                pos = moods.indexOfFirst { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }
                posLast = moods.indexOfLast { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }
            }
            "Last Year" -> {
                maxDate = LocalDate.parse("${LocalDate.now().year}-01-01", format)
                minDate = LocalDate.parse("${LocalDate.now().year - 2}-12-31", format)
                pos = moods.indexOfFirst { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }
                posLast = moods.indexOfLast { LocalDate.parse(it.date!!, format) < maxDate && LocalDate.parse(it.date, format) > minDate }
            }
            "Years Ago" -> {
                maxDate = LocalDate.parse("${LocalDate.now().year - 1}-01-01", format)
                pos = moods.indexOfFirst { LocalDate.parse(it.date!!, format) < maxDate }
            }
        }

        if (pos != -1) {
            moods.add(pos, MoodEntryModel("2222-01-01","12:01",Mood("5"),ArrayList(),ArrayList(),title))
            if (posLast != -1) moods.add((posLast + 2), MoodEntryModel("2222-01-01","12:01",Mood("5"),ArrayList(),ArrayList(),""))
        }

        moodList.clear()
        for (i in moods.indices) {
            when {
                moods[i].key == title -> moodList.add(FilterEntryModel(title))
                moods[i].key == "" -> moodList.add(FilterEntryModel(""))
                validKeys.contains(moods[i].key) -> moodList.add(FilterEntryModel(moods[i].key.toString()))
                else -> moodList.add(moods[i])
            }
            // Prevent two filter rows one after the other
            if (i < moodList.size)
                if( moodList[i].viewType == RowEntryModel.FILTER_ENTRY_TYPE)
                    if (i > 0)
                        if (moodList[i - 1].key != "")
                            if (moodList[i -1].viewType == RowEntryModel.FILTER_ENTRY_TYPE) moodList.removeAt(i - 1)
        }
        notifyDataSetChanged()
    }

    private fun updateDateText(calendar: Calendar, holder: MoodEntryViewHolder, mood: MoodEntryModel) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        holder.dateText.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        holder.timeText.text = timeFormat.format(calendar.time)

        val newMood = MoodEntryModel(dateFormat.format(calendar.time), timeFormat.format(calendar.time),mood.mood,mood.feelings,mood.activities,mood.key)

        updateMoodEntry(newMood)
    }

    override fun getItemViewType(position: Int): Int {
        return moodList[position].viewType
    }

    private fun getSanitisedNumber(value: Int): Int {
        return (ceil(
            value.toDouble().div(settings.moodMax / 5)
        ).toInt())
    }


    private fun getEmoji(convertValue: String): Int {
        return when (convertValue) {
            "Ecstatic" -> R.string.mood_ecstatic
            "Happy" -> R.string.mood_happy
            "Unhappy" -> R.string.mood_unhappy
            "Terrible" -> R.string.mood_terrible
            else -> R.string.mood_average
        }
    }

    private fun bindToMoodViewHolder(mHolder: MoodEntryViewHolder, position: Int) {
        val moodViewHolder = moodList[position] as MoodEntryModel
        if (moodViewHolder.key == "default_row_key") return

        mHolder.dateText.text = moodViewHolder.date
        mHolder.timeText.text = moodViewHolder.time

        if (settings.moodMode == Mood.MOOD_MODE_FACES)
            mHolder.moodText.text = mHolder.itemView.resources.getString(
                getEmoji(
                    moodViewHolder.mood!!.toFaces(
                        getSanitisedNumber(moodViewHolder.mood.value!!.toInt()).toString())))
        else mHolder.moodText.text = moodViewHolder.mood!!.value
        mHolder.activityText.text = when (moodViewHolder.activities.toString()) {
            "[]" ->  "Click to add an activity"
            else -> moodViewHolder.activities.toString().removeSurrounding(
                "[",
                "]"
            )
        }

        mHolder.feelingsText.text = when (moodViewHolder.feelings.toString()) {
            "[]" ->  "Click to add feelings"
            else -> moodViewHolder.feelings.toString().removeSurrounding(
                "[",
                "]"
            )
        }

        if (settings.moodMode == Mood.MOOD_MODE_NUMBERS) {
            when {
                mHolder.moodText.text.toString()
                    .toInt() > 3 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_high)
                mHolder.moodText.text.toString()
                    .toInt() < 3 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour_low)
                mHolder.moodText.text.toString()
                    .toInt() == 3 -> mHolder.moodText.setBackgroundResource(R.drawable.mood_rating_colour)
            }
        } else mHolder.moodText.setBackgroundResource(0)

        val calendar
                : Calendar = Calendar.getInstance(TimeZone.getDefault())

        mHolder.moodText.setOnClickListener {
            onMoodValueClicked(moodList[position] as MoodEntryModel)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateDateText(calendar, mHolder, moodViewHolder)
        }

        val dateSetListener =
            DatePickerDialog.OnDateSetListener { _, year, month, day ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, day)

                TimePickerDialog(
                    mHolder.itemView.context,
                    timeSetListener,
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()

            }

        mHolder.dateText.setOnClickListener {
            DatePickerDialog(
                mHolder.itemView.context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        mHolder.timeText.setOnClickListener {
            DatePickerDialog(
                mHolder.itemView.context, dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        mHolder.activityText.setOnClickListener {
            onStartActivitiesActivity(moodList[position] as MoodEntryModel)
        }

        mHolder.feelingsText.setOnClickListener {
            if (moodList[position].viewType == RowEntryModel.MOOD_ENTRY_TYPE) {
                startFeelingsActivity(moodList[position] as MoodEntryModel)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        when (holder.itemViewType) {
            RowEntryModel.MOOD_ENTRY_TYPE -> bindToMoodViewHolder(holder as MoodEntryViewHolder, position)
            RowEntryModel.FILTER_ENTRY_TYPE -> {
                val vHolder = holder as FilterViewHolder
                val filterEntry = moodList[position] as FilterEntryModel
                vHolder.tvFilterTitle.text = filterEntry.title
            }
        }
    }

    override fun getItemCount(): Int {
        return moodList.size
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return false
    }

    fun getItem(position: Int): RowEntryModel? {
        if (position < moodList.size) return moodList[position]
        return null
    }

    override fun onItemDismiss(position: Int) {
        val moodEntry = moodList[position]
        moodList.removeAt(position)

        run { notifyItemRemoved(position) }
        val listToSave = ArrayList<MoodEntryModel>()
        for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
        onSwiped(moodEntry as MoodEntryModel, listToSave)
    }
}