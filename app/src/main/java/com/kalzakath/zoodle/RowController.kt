package com.kalzakath.zoodle

import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.DataControllerEventListener
import java.util.logging.Logger

class RowController: DataController {
    private var _rowEntryList = arrayListOf<RowEntryModel>()
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    private val listeners = ArrayList<DataControllerEventListener>()

    override fun registerForUpdates(listener: DataControllerEventListener) {
        listeners.add(listener)
    }

    override fun unregisterForUpdates(listener: DataControllerEventListener) {
        listeners.remove(listener)
    }


    private fun callChangeEvent(data: ArrayList<RowEntryModel>, eventType: Int) {
        val event = RowControllerEvent(data, eventType)
        listeners.forEach { it.onUpdateFromDataController(event) }
        log.info("Call event triggered")
    }

    override fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        _rowEntryList.add(rowEntryModel)
        sort()

        if (callUpdate) {
            log.info("Row added")
            callChangeEvent(_rowEntryList, RowControllerEvent.ADDITION )
        }
    }

    override fun add(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to add ${rowEntryList.size} rows")

        for(i in rowEntryList.indices) {
            val rw = rowEntryList[i]
            val index = _rowEntryList.indices.find {
                rw.key == _rowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, rw)
            else add(rw)
        }
        sort()
        callChangeEvent(_rowEntryList, RowControllerEvent.ADDITION )
    }

    override fun removeAt(position: Int, callUpdate: Boolean) {
        log.info("Removing row at position $position\n${_rowEntryList[position].toMap()}")
        _rowEntryList.removeAt(position)
        sort()
        if (callUpdate) callChangeEvent(_rowEntryList, RowControllerEvent.REMOVE )
    }

    override fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        val index = indexOf(rowEntryModel)
        if (index != -1) removeAt(index, callUpdate)
    }

    override fun remove(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to remove rows...")
        rowEntryList.forEach { toDelete -> _rowEntryList.find { toDelete.key == it.key } ?.let { remove(it, false) } }
        sort()
        callChangeEvent(_rowEntryList, RowControllerEvent.REMOVE )
    }

    override fun update(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        log.info("Attempting to update row ${rowEntryModel.toMap()}")
        val index = indexOf(rowEntryModel)
        if (index != -1) updateAt(index, rowEntryModel, callUpdate)
        else add(rowEntryModel)
    }

    override fun updateAt(position: Int, rowEntryModel: RowEntryModel, callUpdate: Boolean): Boolean {
        if (position > size()) return false
        log.info("Updating row at position $position")

        when (val toUpdateRow = get(position)) {
            is MoodEntryModel -> {
                val moodEntryUpdated = MoodEntryHelper.convertStringToDateTime(toUpdateRow.lastUpdated)

                val newEntryModel = rowEntryModel as MoodEntryModel
                val newMoodEntryUpdated = MoodEntryHelper.convertStringToDateTime(newEntryModel.lastUpdated)

                if (newMoodEntryUpdated >= moodEntryUpdated) {
                    _rowEntryList[position] = rowEntryModel
                    if (callUpdate) {
                        sort()
                        callChangeEvent(_rowEntryList, RowControllerEvent.UPDATE )
                    }
                    return true
                }
            }
        }

        return false
    }

    private fun sort() {
        val comparator = compareBy { row: RowEntryModel ->
            MoodEntryHelper.convertStringToDateTime(
                row.date + "T" + row.time,
                16
            )
        }.reversed()
        val sorted = _rowEntryList.sortedWith(comparator)
        val oldList: ArrayList<RowEntryModel> = arrayListOf()
        oldList.addAll(_rowEntryList)

        _rowEntryList.clear()
        _rowEntryList.addAll(sorted)

        /* Not implemented
        _rowEntryList.indices.forEach {
            val index = oldList.indexOf(_rowEntryList[it])
            if (index != it) callChangeEvent(arrayListOf(_rowEntryList[it]), RowControllerEvent.UPDATE)
        }

         */
    }

    override fun update(updateRowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to update list of size: ${updateRowEntryList.size}")

        for(updateRow in updateRowEntryList) {
            val index = _rowEntryList.indices.find {
                updateRow.key == _rowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, updateRow, false)
            else add(updateRow, false)
        }
        sort()
        callChangeEvent(_rowEntryList, RowControllerEvent.UPDATE )
    }

    override fun size(): Int { return _rowEntryList.size }

    override fun indexOf(rowEntryModel: RowEntryModel): Int {
        val moody = rowEntryModel as MoodEntryModel
        log.info("Looking for key ${moody.key}")
        return _rowEntryList.filterIsInstance<MoodEntryModel>().indexOfFirst {
            it.key == moody.key }
    }

    override fun get(position: Int): RowEntryModel { return _rowEntryList[position] }

    override fun <T> find(type: String, condition: T): RowEntryModel? {
        val found: MoodEntryModel? = _rowEntryList.filterIsInstance<MoodEntryModel>().let {
            it.find { mood -> mood.toMap()[type.lowercase()] == condition }
        }
        log.info("Looking for $type as ${condition.toString()}")
        return found
    }
}