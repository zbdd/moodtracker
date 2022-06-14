package com.kalzakath.zoodle

import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.DataControllerEventListener
import java.util.logging.Logger

class RowController: DataController {
    override var mainRowEntryList = arrayListOf<RowEntryModel>()
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    private val listeners = ArrayList<DataControllerEventListener>()

    override fun registerForUpdates(listener: DataControllerEventListener) {
        listeners.add(listener)
    }

    override fun unregisterForUpdates(listener: DataControllerEventListener) {
        listeners.remove(listener)
    }

    private fun callChangeEvent(data: ArrayList<RowEntryModel>, eventType: Int) {
        mainRowEntryList = sort(mainRowEntryList)
        val event = RowControllerEvent(sort(data), eventType)
        listeners.forEach { it.onUpdateFromDataController(event) }
    }

    override fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        mainRowEntryList.add(rowEntryModel)

        if (callUpdate) {
            callChangeEvent(arrayListOf(rowEntryModel), RowControllerEvent.ADDITION )
        }
    }

    override fun add(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to add ${rowEntryList.size} rows")

        for(i in rowEntryList.indices) {
            val rw = rowEntryList[i]
            val index = mainRowEntryList.indices.find {
                rw.key == mainRowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, rw, false)
            else add(rw, false)
        }
        callChangeEvent(mainRowEntryList, RowControllerEvent.ADDITION )
    }

    override fun removeAt(position: Int, callUpdate: Boolean) {
        val toRemove = mainRowEntryList[position]
        log.info("Removing row at position $position\n${mainRowEntryList[position].toMap()}")
        mainRowEntryList.removeAt(position)
        if (callUpdate) callChangeEvent(arrayListOf(toRemove), RowControllerEvent.REMOVE )
    }

    override fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        val index = indexOf(rowEntryModel)
        if (index != -1) removeAt(index, callUpdate)
    }

    override fun remove(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to remove rows...")
        rowEntryList.forEach { toDelete -> mainRowEntryList.find { toDelete.key == it.key } ?.let { remove(it, false) } }
        callChangeEvent(rowEntryList, RowControllerEvent.REMOVE )
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
                    mainRowEntryList[position] = rowEntryModel
                    if (callUpdate) {
                        callChangeEvent(arrayListOf(rowEntryModel), RowControllerEvent.UPDATE )
                    }
                    return true
                }
            }
        }

        return false
    }

    private fun sort(arrayList: ArrayList<RowEntryModel>): ArrayList<RowEntryModel> {
        val comparator = compareBy { row: RowEntryModel ->
            MoodEntryHelper.convertStringToDateTime(
                row.date + "T" + row.time,
                16
            )
        }.reversed()
        val sorted = arrayList.sortedWith(comparator)
        val arrayListToReturn = ArrayList<RowEntryModel>()
        arrayListToReturn.addAll(sorted)

        return arrayListToReturn

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
            val index = mainRowEntryList.indices.find {
                updateRow.key == mainRowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, updateRow, false)
            else add(updateRow, false)
        }
        callChangeEvent(updateRowEntryList, RowControllerEvent.UPDATE )
    }

    override fun size(): Int { return mainRowEntryList.size }

    override fun indexOf(rowEntryModel: RowEntryModel): Int {
        val moody = rowEntryModel as MoodEntryModel
        log.info("Looking for key ${moody.key}")
        return mainRowEntryList.filterIsInstance<MoodEntryModel>().indexOfFirst {
            it.key == moody.key }
    }

    override fun get(position: Int): RowEntryModel { return mainRowEntryList[position] }

    override fun <T> find(type: String, condition: T): RowEntryModel? {
        val found: MoodEntryModel? = mainRowEntryList.filterIsInstance<MoodEntryModel>().let {
            it.find { mood -> mood.toMap()[type.lowercase()] == condition }
        }
        log.info("Looking for $type as ${condition.toString()}")
        return found
    }
}