package com.kalzakath.zoodle

import java.util.logging.Logger

class RowController(recyclerViewAdaptor: RecyclerViewAdaptor,
private var onDataChangeEvent: (RowControllerEvent) -> Unit) {
    private var _rowEntryList = arrayListOf<RowEntryModel>()
    private val _rvAdaptor = recyclerViewAdaptor
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

    init {
        _rvAdaptor.moodList = _rowEntryList
        _rvAdaptor.connectController(this)
    }

    fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean = true) {
        log.info("Row added")
        _rowEntryList.add(rowEntryModel)
        _rvAdaptor.notifyItemInserted(_rowEntryList.size)

        if (callUpdate) {
            onDataChangeEvent(RowControllerEvent(_rowEntryList, RowControllerEvent.ADDITION ) )
            sort()
        }
    }

    fun add(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to add rows...")
        for(i in rowEntryList.indices) {
            val rw = rowEntryList[i]
            val index = _rowEntryList.indices.find {
                rw.key == _rowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, rw)
            else add(rw)
        }
        onDataChangeEvent(RowControllerEvent(_rowEntryList, RowControllerEvent.ADDITION ) )
        sort()
    }

    fun removeAt(position: Int, callUpdate: Boolean = true) {
        _rvAdaptor.notifyItemRemoved(position)
        _rowEntryList.removeAt(position)
        if (callUpdate) onDataChangeEvent(RowControllerEvent(_rowEntryList, RowControllerEvent.REMOVE ) )
    }

    fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean = true) {
        val index = getIndex(rowEntryModel)
        if (index != -1) removeAt(index, callUpdate)
    }

    fun remove(rowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to remove rows...")
        rowEntryList.forEach { toDelete -> _rowEntryList.find { toDelete.key == it.key } ?.let { remove(it, false) } }
        onDataChangeEvent(RowControllerEvent(_rowEntryList, RowControllerEvent.REMOVE ) )
    }

    fun update(rowEntryModel: RowEntryModel, callUpdate: Boolean = true) {
        val index = indexOf(rowEntryModel)
        if (index != -1) updateAt(index, rowEntryModel, callUpdate)
    }

    fun updateAt(position: Int, rowEntryModel: RowEntryModel, callUpdate: Boolean = true): Boolean {
        if (position > size()) return false

        when (val toUpdateRow = get(position)) {
            is MoodEntryModel -> {
                val moodEntryUpdated = MoodEntryHelper.convertStringToDateTime(toUpdateRow.lastUpdated)

                val newEntryModel = rowEntryModel as MoodEntryModel
                val newMoodEntryUpdated = MoodEntryHelper.convertStringToDateTime(newEntryModel.lastUpdated)

                if (newMoodEntryUpdated > moodEntryUpdated) {
                    _rowEntryList[position] = rowEntryModel
                    _rvAdaptor.notifyItemChanged(position)
                    if (callUpdate) {
                        onDataChangeEvent(RowControllerEvent(_rowEntryList, RowControllerEvent.UPDATE ) )
                        sort()
                    }
                    return true
                }
            }
        }

        return false
    }

    fun sort() {
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
            if (index != it) _rvAdaptor.notifyItemMoved(it, index)
        }
    }

    fun update(updateRowEntryList: ArrayList<RowEntryModel>) {
        log.info("Attempting to update list of size: ${updateRowEntryList.size}")

        for(updateRow in updateRowEntryList) {
            val index = _rowEntryList.indices.find {
                updateRow.key == _rowEntryList[it].key
            }
            if (index != null && index != -1) updateAt(index, updateRow, false)
            else add(updateRow, false)
        }
        onDataChangeEvent(RowControllerEvent(_rowEntryList, RowControllerEvent.UPDATE ) )
        sort()
    }

    fun size(): Int { return _rowEntryList.size }

    fun indexOf(rowEntryModel: RowEntryModel): Int {
        return _rowEntryList.indexOfFirst { it.key == rowEntryModel.key }
    }

    fun get(position: Int): RowEntryModel { return _rowEntryList[position] }

    fun getIndex(rowEntryModel: RowEntryModel): Int {
        return _rowEntryList.indices.find { _rowEntryList[it].key == rowEntryModel.key } ?: -1
    }
}