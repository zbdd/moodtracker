package com.kalzakath.zoodle

open class RowEntryModel () {
    @Transient open var key: String? = "default_row_key"
    @Transient open var viewType: Int = -1

    companion object {
        const val MOOD_ENTRY_TYPE: Int = 0
        const val FILTER_ENTRY_TYPE: Int = 1
    }
}