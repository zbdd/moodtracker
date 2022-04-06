package com.example.rows

class FilterEntryModel (
    var title: String = "",
        ): RowEntryModel() {
            init {
                viewType = FILTER_ENTRY_TYPE
            }
        }