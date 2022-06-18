package com.kalzakath.zoodle

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.DataControllerEventListener
import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.FilterEntryModel
import com.kalzakath.zoodle.model.MoodEntryModel
import com.kalzakath.zoodle.model.bindToViewHolder
import com.kalzakath.zoodle.model.hideRow
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger
import java.util.stream.IntStream

interface RecycleRowOnEvent {
    var onLongPress: ((MoodEntryModel) -> Unit)?
}

@SuppressLint("NotifyDataSetChanged")
class RecyclerViewAdaptor(
    val onMoodValueClicked: (MoodEntryModel) -> Unit,
    val onStartActivitiesActivity: (MoodEntryModel) -> Unit,
    val startFeelingsActivity: (MoodEntryModel) -> Unit,
    private val rowController: DataController
):
    Adapter<ViewHolder>(), SwipeHelperCallback.ItemTouchHelperAdaptor, RecycleRowOnEvent, DataControllerEventListener {

    override var onLongPress: ((MoodEntryModel) -> Unit)? = null
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    var moodList: ArrayList<RowEntryModel> = arrayListOf()
    lateinit var viewHolder: ViewHolder

    init {
        rowController.registerForUpdates(this)
        onUpdateFromDataController(RowControllerEvent())
    }

    override fun onUpdateFromDataController(event: RowControllerEvent) {
        moodList.clear()
        moodList.addAll(rowController.mainRowEntryList)
        addFilterView()
        notifyDataSetChanged()
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
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) < maxDate
                                && LocalDate.parse((moodList[it] as MoodEntryModel).date, format) > minDate }
                        .findFirst()
                    posLast = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) >= maxDate
                                && LocalDate.parse((moodList[it] as MoodEntryModel).date, format) <= minDate }
                        .findFirst()
                }
                "Last Year" -> {
                    maxDate = LocalDate.parse("${LocalDate.now().year}-01-01", format)
                    minDate = LocalDate.parse("${LocalDate.now().year - 2}-12-31", format)
                    pos = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) < maxDate
                                && LocalDate.parse((moodList[it] as MoodEntryModel).date, format) > minDate }
                        .findFirst()
                    posLast = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) >= maxDate
                                && LocalDate.parse((moodList[it] as MoodEntryModel).date, format) <= minDate }
                        .findFirst()
                }
                "Years Ago" -> {
                    maxDate = LocalDate.parse("${LocalDate.now().year - 1}-01-01", format)
                    pos = IntStream.range(0, moodList.size-1)
                        .filter { moodList[it].viewType == MoodEntryModel().viewType }
                        .filter { LocalDate.parse((moodList[it] as MoodEntryModel).date, format) < maxDate }
                        .findFirst()
                }
            }

            if (pos != OptionalInt.empty()) {
                moodList.add(pos.asInt, FilterEntryModel(
                    title,
                    moodList[pos.asInt].date,
                    MoodEntryHelper.convertStringToTime(moodList[pos.asInt].time).plusMinutes(1).toString())
                )
                notifyItemInserted(pos.asInt)
                pos = OptionalInt.of(pos.asInt + 1)

                if (posLast != OptionalInt.empty()) {

                    posLast = OptionalInt.of(posLast.asInt + 1)
                    if (posLast == OptionalInt.of(pos.asInt + 1)) posLast = OptionalInt.of(pos.asInt + 1)

                    moodList.add(posLast.asInt, FilterEntryModel("",
                        moodList[posLast.asInt].date,
                        MoodEntryHelper.convertStringToTime(moodList[posLast.asInt].time).plusMinutes(1).toString())
                    )
                    notifyItemInserted(posLast.asInt)
                } else { moodList.add(
                    FilterEntryModel("",
                    moodList[moodList.size-1].date,
                    MoodEntryHelper.convertStringToTime(moodList[moodList.size-1].time).minusMinutes(1).toString())
                )
                    notifyItemInserted(moodList.size-1)
                }
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
                                notifyItemRemoved(i-1)
                            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return moodList[position].viewType
    }

    private fun updateDateText(calendar: Calendar, holder: MoodViewHolder, mood: MoodEntryModel) {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        holder.dateText.text = dateFormat.format(calendar.time)

        val timeFormat = SimpleDateFormat("HH:mm", Locale.ENGLISH)
        holder.timeText.text = timeFormat.format(calendar.time)

        mood.date = dateFormat.format(calendar.time)
        mood.time = timeFormat.format(calendar.time)

        rowController.update(mood)
    }

    private fun toggleFilterView(position: Int) {
        for (i in position + 1 until moodList.size) {
            val row = moodList[i]
            if (row.viewType == MoodEntryModel().viewType) {
                val mood = row as MoodEntryModel
                mood.isVisible = !mood.isVisible

                val viewHolder = mood.viewHolder
                if (viewHolder != null) {
                    val mVH = viewHolder as MoodViewHolder
                    mood.hideRow(mVH)
                }
            }else {
                break
            }
        }
    }

    private fun initButtons(viewHolder: ViewHolder, row: RowEntryModel) {

        when (row.viewType) {
            FilterEntryModel().viewType -> {
                (viewHolder as FilterViewHolder).tvFilterTitle.setOnClickListener {
                    toggleFilterView(moodList.indexOf(row))
                }
            }
            MoodEntryModel().viewType -> {
                val moodEntry = row as MoodEntryModel
                val mHolder = viewHolder as MoodViewHolder

                val dtPicker = DateTimePicker()
                dtPicker.onUpdateListener = {
                    updateDateText(it, mHolder, moodEntry)
                }

                mHolder.moodText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }
                mHolder.moodText.setOnClickListener {
                    onMoodValueClicked(moodEntry)
                }

                mHolder.dateText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.timeText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }

                mHolder.activityText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }
                mHolder.activityText.setOnClickListener {
                    onStartActivitiesActivity(moodEntry)
                }

                mHolder.feelingsText.setOnLongClickListener {
                    onLongPress?.invoke(row)
                    return@setOnLongClickListener true
                }
                mHolder.feelingsText.setOnClickListener {
                    startFeelingsActivity(moodEntry)
                }

                mHolder.dateText.setOnClickListener {
                    dtPicker.show(mHolder.itemView.context)
                }

                mHolder.timeText.setOnClickListener {
                    dtPicker.show(mHolder.itemView.context)
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (moodList[position].viewType) {
            MoodEntryModel().viewType -> (moodList[position] as MoodEntryModel).bindToViewHolder(holder)
            FilterEntryModel().viewType -> (moodList[position] as FilterEntryModel).bindToViewHolder(holder)
        }
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
        rowController.remove(moodList[position])
    }
}