package com.kalzakath.zoodle.interfaces

import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.kalzakath.zoodle.RowEntryModel

interface FirebaseAuthentication {
    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult): FirebaseUser?
}

interface OnlineDataHandlerEventListener {
    fun onUpdateFromDatabase(data: ArrayList<RowEntryModel>)
    fun onLoginUpdateFromDatabase(result: String)
}

interface OnlineDataHandlerEventHandler {
    fun registerForUpdates(listener: OnlineDataHandlerEventListener)
    fun unregisterForUpdates(listener: OnlineDataHandlerEventListener)
}

interface OnlineDataHandler: OnlineDataHandlerEventHandler {

    fun write(moods: ArrayList<RowEntryModel>)
    fun read(): ArrayList<RowEntryModel>
    fun remove(data: ArrayList<RowEntryModel>)

}