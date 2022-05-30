package com.kalzakath.zoodle.interfaces

import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.kalzakath.zoodle.MoodEntryModel
import com.kalzakath.zoodle.RowEntryModel

interface OnlineDataHandler {
    fun onItemDismissed(moodEntry: MoodEntryModel)

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult): FirebaseUser?

    fun write(moods: ArrayList<MoodEntryModel>)
    fun read(user: FirebaseUser?): ArrayList<RowEntryModel>

}