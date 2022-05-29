package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import java.util.stream.IntStream

class RecyclerViewAdaptor(
    val onSwiped: (MoodEntryModel, ArrayList<MoodEntryModel>) -> Unit,
    val onListUpdated: (ArrayList<MoodEntryModel>) -> Unit,
    val onMoodValueClicked: (MoodEntryModel, RecyclerViewAdaptor) -> Unit,
    val onStartActivitiesActivity: (MoodEntryModel) -> Unit,
    val startFeelingsActivity: (MoodEntryModel) -> Unit,
    private val rowController: DataController):
    Adapter<ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor {

    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    var moodList: ArrayList<RowEntryModel> = arrayListOf()
    lateinit var viewHolder: ViewHolder

    init {
        rowController.onDataChangeListener = {
            moodList.clear()
            moodList.addAll(it.data)
            addFilterView()
            notifyDataSetChanged()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val viewFactory = RowViewFactory()
        viewHolder = viewFactory.createView(parent, viewType)
        return viewHolder
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun addFilterView() {
        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH)
        if (moodList.isEmpty()) return

        var maxDate: LocalDate = LocalDate.now()
        var minDate: LocalDate
        var pos: OptionalInt = OptionalInt.empty()
        var posLast: OptionalInt = OptionalInt.empty()

        val arrayDelete = arrayListOf<RowEntryModel>()
        moodList.forEach { if (it is FilterEntryModel) { log.info("Deleting FEM"); arrayDelete.add(it) }}
        arrayDelete.forEach { moodList.remove(it) }

        log.info("Adding FilterEntryViews")

        for (title in arrayListOf("Today", "Last Week", "Last Month", "Last Year", "Years Ago")) {
            when (title) {
                "Today" -> {
                    pos = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) == maxDate }
                        .findFirst()
                    posLast = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) != maxDate }
                        .findFirst()
                }
                "Last Week" -> {
                    maxDate = LocalDate.parse("${LocalDate.now().minusWeeks(1)}", format)
                    minDate = LocalDate.parse("${LocalDate.now().minusWeeks(2)}", format)

                    pos = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date
                            , format) < maxDate
                                && LocalDate.parse((moodList[it] as MoodEntryModel).date, format) > minDate }
                        .findFirst()
                    posLast = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) >= maxDate
                                && LocalDate.parse((moodList[it] as MoodEntryModel).date, format) <= minDate }
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
                moodList.add(pos.asInt, FilterEntryModel(
                    title,
                    moodList[pos.asInt].date,
                    MoodEntryHelper.convertStringToTime(moodList[pos.asInt].time).plusMinutes(1).toString()))
                pos = OptionalInt.of(pos.asInt + 1)

                if (posLast != OptionalInt.empty()) {

                    posLast = OptionalInt.of(posLast.asInt + 1)
                    if (posLast == OptionalInt.of(pos.asInt + 1)) posLast = OptionalInt.of(pos.asInt + 1)

                    moodList.add(posLast.asInt, FilterEntryModel("",
                        moodList[posLast.asInt].date,
                        MoodEntryHelper.convertStringToTime(moodList[posLast.asInt].time).plusMinutes(1).toString()))
                } else { moodList.add(FilterEntryModel("",
                    moodList[moodList.size-1].date,
                    MoodEntryHelper.convertStringToTime(moodList[moodList.size-1].time).minusMinutes(1).toString())) }
            }
        }

        for (i in moodList.indices) {
            // Prevent two filter rows one after the other
            if (i < moodList.size)
                if( moodList[i].viewType == FilterEntryModel().viewType)
                    if (i > 0)
                        if (moodList[i - 1].key != "")
                            if (moodList[i -1].viewType == FilterEntryModel().viewType) {
                                moodList.removeAt(i - 1)
                            }
        }
    }

    private fun updateDateText(calendar: Calendar, holder: MoodViewHolder, mood: MoodEntryModel) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        holder.dateText.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        holder.timeText.text = timeFormat.format(calendar.time)

        val newMood = MoodEntryModel(dateFormat.format(calendar.time), timeFormat.format(calendar.time),mood.mood,mood.feelings,mood.activities,mood.key)
        rowController.update(newMood)
        //updateMoodEntry(newMood)
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
            onMoodValueClicked(moodEntry, this)
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
        rowController.removeAt(position)
    }
}