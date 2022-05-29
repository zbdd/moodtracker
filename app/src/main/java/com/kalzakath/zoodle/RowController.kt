package com.kalzakath.zoodle

import java.util.logging.Logger

interface DataController: DataControllerAccessors, DataControllerEventHandlers

interface DataControllerEventHandlers {
    var onDataChangeListener: ((RowControllerEvent)->Unit)?
}

interface DataControllerAccessors {
    fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun add(rowEntryList: ArrayList<RowEntryModel>)

    fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun remove(rowEntryList: ArrayList<RowEntryModel>)
    fun removeAt(position: Int, callUpdate: Boolean = true)

    fun update(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun update(updateRowEntryList: ArrayList<RowEntryModel>)
    fun updateAt(position: Int, rowEntryModel: RowEntryModel, callUpdate: Boolean = true): Boolean

    fun size(): Int

    fun indexOf(rowEntryModel: RowEntryModel): Int
    fun get(position: Int): RowEntryModel
}

class RowController: DataController {
    private var _rowEntryList = arrayListOf<RowEntryModel>()
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")
    override var onDataChangeListener: ((RowControllerEvent) -> Unit)? = null

    private fun callChangeEvent(data: ArrayList<RowEntryModel>, eventType: Int) {
        onDataChangeListener?.invoke(RowControllerEvent(_rowEntryList, eventType))
    }

    override fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        log.info("Row added")
        _rowEntryList.add(rowEntryModel)

        if (callUpdate) {
            callChangeEvent(_rowEntryList, RowControllerEvent.ADDITION )
            sort()
        }
    }

    override fun add(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to add rows...")
        for(i in rowEntryList.indices) {
            val rw = rowEntryList[i]
            val index = _rowEntryList.indices.find {
                rw.key == _rowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, rw)
            else add(rw)
        }
        callChangeEvent(_rowEntryList, RowControllerEvent.ADDITION )
        sort()
    }

    override fun removeAt(position: Int, callUpdate: Boolean) {
        _rowEntryList.removeAt(position)
        if (callUpdate) callChangeEvent(_rowEntryList, RowControllerEvent.REMOVE )
    }

    override fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        val index = indexOf(rowEntryModel)
        if (index != -1) removeAt(index, callUpdate)
    }

    override fun remove(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to remove rows...")
        rowEntryList.forEach { toDelete -> _rowEntryList.find { toDelete.key == it.key } ?.let { remove(it, false) } }
        callChangeEvent(_rowEntryList, RowControllerEvent.REMOVE )
    }

    override fun update(rowEntryModel: RowEntryModel, callUpdate: Boolean) {
        val index = indexOf(rowEntryModel)
        if (index != -1) updateAt(index, rowEntryModel, callUpdate)
        else add(rowEntryModel)
    }

    override fun updateAt(position: Int, rowEntryModel: RowEntryModel, callUpdate: Boolean): Boolean {
        if (position > size()) return false

        when (val toUpdateRow = get(position)) {
            is MoodEntryModel -> {
                val moodEntryUpdated = MoodEntryHelper.convertStringToDateTime(toUpdateRow.lastUpdated)

                val newEntryModel = rowEntryModel as MoodEntryModel
                val newMoodEntryUpdated = MoodEntryHelper.convertStringToDateTime(newEntryModel.lastUpdated)

                if (newMoodEntryUpdated > moodEntryUpdated) {
                    _rowEntryList[position] = rowEntryModel
                    if (callUpdate) {
                        callChangeEvent(_rowEntryList, RowControllerEvent.UPDATE )
                        sort()
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

        _rowEntryList.indices.forEach {
            val index = oldList.indexOf(_rowEntryList[it])
            if (index != it) callChangeEvent(arrayListOf(_rowEntryList[it]), RowControllerEvent.UPDATE)
        }
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
        callChangeEvent(_rowEntryList, RowControllerEvent.UPDATE )
        sort()
    }

    override fun size(): Int { return _rowEntryList.size }

    override fun indexOf(rowEntryModel: RowEntryModel): Int {
        return _rowEntryList.indexOfFirst { it.key == rowEntryModel.key }
    }

    override fun get(position: Int): RowEntryModel { return _rowEntryList[position] }
}