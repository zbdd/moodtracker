package com.kalzakath.zoodle

class FilterEntryModel (
    var title: String = "",
    override var key: String = "default_row_key",
    override var viewType: Int = FILTER_ENTRY_TYPE
        ): RowEntryModel(key, viewType)