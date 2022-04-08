package com.example.rows

class FilterEntryModel (
    var title: String = "",
        ): RowEntryModel() {
            override var key: String = "default_row_key"
    override var viewType: Int = FILTER_ENTRY_TYPE
        }