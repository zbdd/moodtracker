package com.kalzakath.zoodle.interfaces

import com.kalzakath.zoodle.RowControllerEvent
import com.kalzakath.zoodle.RowEntryModel

interface DataController: DataControllerAccessors, DataControllerEventHandlers

interface DataControllerEventHandlers {
    var onDataChangeListener: ((RowControllerEvent)->Unit)?
}

interface DataControllerAccessors {
    fun add(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun add(rowEntryList: ArrayList<RowEntryModel>)

    fun remove(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun remove(rowEntryList: ArrayList<RowEntryModel>)
    fun removeAt(position: Int, callUpdate: Boolean = true)

    fun update(rowEntryModel: RowEntryModel, callUpdate: Boolean = true)
    fun update(updateRowEntryList: ArrayList<RowEntryModel>)
    fun updateAt(position: Int, rowEntryModel: RowEntryModel, callUpdate: Boolean = true): Boolean

    fun size(): Int

    fun indexOf(rowEntryModel: RowEntryModel): Int
    fun get(position: Int): RowEntryModel

    fun <T> find(type: String, condition: T): RowEntryModel?
}