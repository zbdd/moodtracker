package com.kalzakath.zoodle.interfaces

import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.kalzakath.zoodle.RowEntryModel

interface OnlineDataHandlerEventListener {
    fun onUpdateFromOnlineDataHandler(data: ArrayList<RowEntryModel>)
}

interface OnlineDataHandlerEventHandler {
    fun registerForUpdates(listener: OnlineDataHandlerEventListener)
    fun unregisterForUpdates(listener: OnlineDataHandlerEventListener)
}

interface OnlineDataHandler: OnlineDataHandlerEventHandler {
    fun remove(data: ArrayList<RowEntryModel>)

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult): FirebaseUser?

    fun write(moods: ArrayList<RowEntryModel>)
    fun read(user: FirebaseUser?): ArrayList<RowEntryModel>

}