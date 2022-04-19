package com.kalzakath.zoodle

import android.os.Parcel
import android.os.Parcelable

class Settings(
    var mood_numerals: String? = "false",
    var mood_max: String? = "5"


) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mood_numerals)
        parcel.writeString(mood_max)
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