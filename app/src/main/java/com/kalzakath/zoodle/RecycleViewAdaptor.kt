package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.stream.IntStream

class RecyclerViewAdaptor(
    val onSwiped: (MoodEntryModel, ArrayList<MoodEntryModel>) -> Unit,
    val onListUpdated: (ArrayList<MoodEntryModel>) -> Unit,
    val onMoodValueClicked: (MoodEntryModel) -> Unit,
    val onStartActivitiesActivity: (MoodEntryModel) -> Unit,
    val startFeelingsActivity: (MoodEntryModel) -> Unit):
    Adapter<ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor {

    private var moodList: ArrayList<RowEntryModel> = ArrayList()
    private var sortBy = "date"
    lateinit var viewHolder: ViewHolder

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewFactory = RowViewFactory()
        viewHolder = viewFactory.createView(parent, viewType)
        return viewHolder
    }

    fun updateListConfig() {
        for (i in moodList.indices) {
            moodList[i].update(Settings)
            notifyItemChanged(i)
        }
    }

    fun updateList(data: ArrayList<MoodEntryModel> = ArrayList(0)) {
        val removeList: MutableList<MoodEntryModel> = ArrayList()
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss", Locale.ENGLISH)

        if (data.isEmpty()) return

        for (moodEntry in data) {
            for (i in moodList.indices) {
                if (moodList[i].viewType == MoodEntryModel().viewType) {
                    val moodListItem = moodList[i] as MoodEntryModel
                    if (moodListItem.key == moodEntry.key) {
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
            if (moodList[i].viewType == MoodEntryModel().viewType) {
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

            if (viewHolder.itemViewType == MoodEntryModel().viewType) {
                val mHolder = viewHolder as MoodViewHolder
                val moodE = moodList[position] as MoodEntryModel
                if (moodE.mood!!.moodMode == Mood.MOOD_MODE_NUMBERS) {
                    moodE.applyDrawable()
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
            if (moodList[i].viewType == MoodEntryModel().viewType) moods.add(moodList[i] as MoodEntryModel)
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
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)

        var maxDate: LocalDate = LocalDate.now()
        val minDate: LocalDate
        var pos: OptionalInt = OptionalInt.empty()
        var posLast: OptionalInt = OptionalInt.empty()

        when (title) {
            "Today" -> {
                pos = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) == maxDate }
                    .findFirst()
                posLast = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) != maxDate }
                    .findFirst()
            }
            "Last Week" -> {
                maxDate = LocalDate.parse("${LocalDate.now().minusWeeks(1)}", format)
                minDate = LocalDate.parse("${LocalDate.now().minusWeeks(2)}", format)

                pos = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) < maxDate
                            && LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) > minDate }
                    .findFirst()
                posLast = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) >= maxDate
                            && LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) <= minDate }
                    .findFirst()
            }
            "Last Month" -> {
                var date: String = LocalDate.now().minusMonths(0).format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-01"
                maxDate = LocalDate.parse(date, format)

                date = LocalDate.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyy-MM")) + "-" + LocalDate.now().minusMonths(1).lengthOfMonth()
                minDate = LocalDate.parse(date, format)
                pos = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) < maxDate
                            && LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) > minDate }
                    .findFirst()
                posLast = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) >= maxDate
                            && LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) <= minDate }
                    .findFirst()
            }
            "Last Year" -> {
                maxDate = LocalDate.parse("${LocalDate.now().year}-01-01", format)
                minDate = LocalDate.parse("${LocalDate.now().year - 2}-12-31", format)
                pos = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) < maxDate
                            && LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) > minDate }
                    .findFirst()
                posLast = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) >= maxDate
                            && LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) <= minDate }
                    .findFirst()
            }
            "Years Ago" -> {
                maxDate = LocalDate.parse("${LocalDate.now().year - 1}-01-01", format)
                pos = IntStream.range(0, moodList.size-1)
                    .filter { moodList[it].viewType == MoodEntryModel().viewType }
                    .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date!!, format) < maxDate }
                    .findFirst()
            }
        }

        if (pos != OptionalInt.empty()) {
            moodList.add(pos.asInt, FilterEntryModel(title))
            pos = OptionalInt.of(pos.asInt + 1)

            if (posLast != OptionalInt.empty()) {

                posLast = OptionalInt.of(posLast.asInt + 1)
                if (posLast == OptionalInt.of(pos.asInt + 1)) posLast = OptionalInt.of(pos.asInt + 1)

                moodList.add(posLast.asInt, FilterEntryModel(""))
            } else moodList.add(FilterEntryModel(""))
        }

        for (i in moodList.indices) {
            // Prevent two filter rows one after the other
            if (i < moodList.size)
                if( moodList[i].viewType == FilterEntryModel().viewType)
                    if (i > 0)
                        if (moodList[i - 1].key != "")
                            if (moodList[i -1].viewType == FilterEntryModel().viewType) moodList.removeAt(i - 1)
        }
        notifyDataSetChanged()
    }

    private fun updateDateText(calendar: Calendar, holder: MoodViewHolder, mood: MoodEntryModel) {
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

    private fun initButtons(viewHolder: ViewHolder, row: RowEntryModel) {

        if (row.viewType != MoodEntryModel().viewType) return

        val moodEntry = row as MoodEntryModel
        val mHolder = viewHolder as MoodViewHolder

        val calendar
                : Calendar = Calendar.getInstance(TimeZone.getDefault())

        mHolder.moodText.setOnClickListener {
            onMoodValueClicked(moodEntry)
        }

        val timeSetListener = TimePickerDialog.OnTimeSetListener { _, hour, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            updateDateText(calendar, mHolder, moodEntry)
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
            onStartActivitiesActivity(moodEntry)
        }

        mHolder.feelingsText.setOnClickListener {
                startFeelingsActivity(moodEntry)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        moodList[position].bindToViewHolder(holder)
        initButtons(holder, moodList[position])
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
        sortList()
        val listToSave = ArrayList<MoodEntryModel>()
        for(item in moodList) { if (item.javaClass == MoodEntryModel::class.java) listToSave.add(item as MoodEntryModel) }
        onSwiped(moodEntry as MoodEntryModel, listToSave)
    }
}