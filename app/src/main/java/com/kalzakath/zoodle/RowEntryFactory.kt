package com.kalzakath.zoodle

open class RowEntryFactory {
    open fun create(viewType: Int): RowEntryModel {
        return when (viewType) {
            FilterEntryModel().viewType -> FilterEntryModel()
            else -> MoodEntryModel()
        }
    }
}