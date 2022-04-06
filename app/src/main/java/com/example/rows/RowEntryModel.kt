package com.example.rows

open class RowEntryModel () {
    open var key: String = ""
    open var viewType: Int = -1

    companion object {
        const val MOOD_ENTRY_TYPE: Int = 0
        const val FILTER_ENTRY_TYPE: Int = 1
    }
}