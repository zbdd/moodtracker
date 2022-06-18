package com.kalzakath.zoodle

import com.kalzakath.zoodle.interfaces.RowEntryModel
import com.kalzakath.zoodle.model.FilterEntryModel
import com.kalzakath.zoodle.model.MoodEntryModel

open class RowEntryFactory {
    open fun create(viewType: Int): RowEntryModel {
        return when (viewType) {
            FilterEntryModel().viewType -> FilterEntryModel()
            else -> MoodEntryModel()
        }
    }
}