package com.kalzakath.zoodle

open class RowEntryModel (
    @Transient open var key: String,
    @Transient open var viewType: Int
) {
    companion object {
        const val MOOD_ENTRY_TYPE: Int = 0
        const val FILTER_ENTRY_TYPE: Int = 1
    }
}