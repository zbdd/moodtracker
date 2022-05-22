package com.kalzakath.zoodle

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class OnlineDataHandler {

    private lateinit var myRef: DatabaseReference
    private var user: FirebaseUser? = null



    private fun onItemDismissed(moodEntry: MoodEntryModel) {
        if (user != null) myRef.child(user?.uid ?: "").child("moodEntries")
            .child(moodEntry.key).removeValue()
    }

    fun onSignInResult(result: FirebaseAuthUIAuthenticationResult): FirebaseUser? {
        user = FirebaseAuth.getInstance().currentUser

        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val database =
                Firebase.database("https://silent-blend-161710-default-rtdb.asia-southeast1.firebasedatabase.app")
            myRef = database.reference
            checkDatabasePathExists()
        }

        return user
    }

    fun write(moods: ArrayList<MoodEntryModel>) {
        checkDatabasePathExists()

        if (user != null) {
            for (moodEntry in moods) {
                val moodHash = moodEntry.toMap()
                val update = hashMapOf<String, Any>("moodEntries/${moodEntry.key}" to moodHash)
                myRef.child(user?.uid ?: "").updateChildren(update)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun read(user: FirebaseUser?): ArrayList<MoodEntryModel> {
        val moodData = ArrayList<MoodEntryModel>()

        myRef.child(user?.uid ?: "").child("moodEntries")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) return

                    if (snapshot.value?.javaClass == HashMap<String, Any>().javaClass) {
                        for ((key, hashmap) in snapshot.value as HashMap<String, HashMap<String, String>>) {
                            moodData.add(
                                MoodEntryModel(
                                    hashmap["date"].toString(),
                                    hashmap["time"].toString(),
                                    Mood(hashmap["mood"].toString()),
                                    hashmap["feelings"] as MutableList<String>,
                                    hashmap["activities"] as MutableList<String>,
                                    key
                                )
                            )
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Debug", "Failed to connect to database")
                }
            })
        return moodData
    }

    private fun checkDatabasePathExists() {
        if (myRef.child(user?.uid ?: "").child("moodEntries") == null) myRef.child(user?.uid ?: "")
            .child("moodEntries").setValue("")
    }
}