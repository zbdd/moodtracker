package com.kalzakath.zoodle

class RowController(recyclerViewAdaptor: RecyclerViewAdaptor) {
    private var _rowEntryList = arrayListOf<RowEntryModel>()
    private val _rvAdaptor = recyclerViewAdaptor

    fun add(rowEntryModel: RowEntryModel) {
        _rowEntryList.add(rowEntryModel)
    }

    fun add(rowEntryList: ArrayList<RowEntryModel>) {
        for(i in rowEntryList.indices) {
            val rw = rowEntryList[i]
            val index = _rowEntryList.indices.find {
                rw.key == _rowEntryList[it].key
            }
            if (index != null){
                updateAt(index, rw)
            } else {
                add(rw)
                _rvAdaptor.notifyItemInserted(_rowEntryList.size)
            }
        }
    }

    fun remove(rowEntryModel: RowEntryModel) {
        _rvAdaptor.notifyItemRemoved(_rowEntryList.size)
        _rowEntryList.remove(rowEntryModel)
    }

    fun remove(rowEntryList: ArrayList<RowEntryModel>) {
        rowEntryList.forEach { toDelete -> _rowEntryList.find { toDelete.key == it.key } ?.let { remove(it) } }
    }

    fun update(rowEntryModel: RowEntryModel) {
        val index = indexOf(rowEntryModel)
        if (index != -1) updateAt(index, rowEntryModel)
    }

    fun updateAt(position: Int, rowEntryModel: RowEntryModel): Boolean {
        if (position > size()) return false

        when (val toUpdateRow = get(position)) {
            is MoodEntryModel -> {
                val moodEntryUpdated = MoodEntryHelper.convertStringToDateTime(toUpdateRow.lastUpdated)

                val newEntryModel = rowEntryModel as MoodEntryModel
                val newMoodEntryUpdated = MoodEntryHelper.convertStringToDateTime(newEntryModel.lastUpdated)

                if (newMoodEntryUpdated > moodEntryUpdated) {
                    _rowEntryList[position] = rowEntryModel
                    _rvAdaptor.notifyItemChanged(position)
                    return true
                }
            }
        }

        return false
    }

    fun update(updateRowEntryList: ArrayList<RowEntryModel>) {
        for(i in _rowEntryList.indices) {
            val rw = _rowEntryList[i]
            val updateRowEntry = updateRowEntryList.find {
                rw.key == it.key
            }
            if (updateRowEntry != null) updateAt(i, updateRowEntry)
        }
    }

    fun size(): Int { return _rowEntryList.size }

    fun indexOf(rowEntryModel: RowEntryModel): Int {
        return _rowEntryList.indexOfFirst { it.key == rowEntryModel.key }
    }

    fun get(position: Int): RowEntryModel { return _rowEntryList[position] }
}