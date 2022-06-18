package com.kalzakath.zoodle

import com.kalzakath.zoodle.interfaces.RowEntryModel

class RowControllerEvent (
    var data: ArrayList<RowEntryModel> = arrayListOf(),
    var type: Int = NOTHING
) {
    companion object {
        const val NOTHING = -1
        const val ADDITION = 0
        const val REMOVE = 1
        const val UPDATE = 2
    }
}