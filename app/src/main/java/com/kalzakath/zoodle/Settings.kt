package com.kalzakath.zoodle

import android.os.Parcel
import android.os.Parcelable

class Settings(
    var moodMode: Int = Mood.MOOD_MODE_NUMBERS,
    var moodMax: Int = 5
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(moodMode)
        parcel.writeInt(moodMax)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Settings> {
        override fun createFromParcel(parcel: Parcel): Settings {
            return Settings(parcel)
        }

        override fun newArray(size: Int): Array<Settings?> {
            return arrayOfNulls(size)
        }
    }
}