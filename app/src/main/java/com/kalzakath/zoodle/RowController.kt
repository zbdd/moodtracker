package com.kalzakath.zoodle

class RowController(recyclerViewAdaptor: RecyclerViewAdaptor) {
    private var _rowEntryList = arrayListOf<RowEntryModel>()
    private val _rvAdaptor = recyclerViewAdaptor

    fun add(rowEntryModel: RowEntryModel) {}

    fun add(rowEntryList: ArrayList<RowEntryModel>) {
        _rowEntryList = rowEntryList
    }

    fun remove(rowEntryModel: RowEntryModel) {}

    fun remove(rowEntryList: ArrayList<RowEntryModel>) {}

    fun update(position: Int, rowEntryModel: RowEntryModel): Boolean {
        if (position > size()) return false

        val toUpdateRow = get(position)

        if (toUpdateRow.viewType == MoodEntryModel().viewType) {
            val toUpdateMood = toUpdateRow as MoodEntryModel
            val moodEntryUpdated = MoodEntryHelper.convertStringToDateTime(toUpdateMood.lastUpdated)
            val newEntryModel = rowEntryModel as MoodEntryModel
            val newMoodEntryUpdated = MoodEntryHelper.convertStringToDateTime(newEntryModel.lastUpdated)

            if (newMoodEntryUpdated > moodEntryUpdated) {
                _rowEntryList[position] = rowEntryModel
                _rvAdaptor.notifyItemChanged(position)
                return true
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
            if (updateRowEntry != null) update(i, updateRowEntry)
        }
    }

    fun size(): Int { return _rowEntryList.size }

    fun get(position: Int): RowEntryModel { return _rowEntryList[position] }
}