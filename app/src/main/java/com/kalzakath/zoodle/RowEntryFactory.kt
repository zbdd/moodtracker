package com.kalzakath.zoodle

class RowEntryFactory {
    fun create(viewType: Int): RowEntryModel {
        return when (viewType) {
            FilterEntryModel().viewType -> FilterEntryModel()
            else -> MoodEntryModel()
        }
    }
}