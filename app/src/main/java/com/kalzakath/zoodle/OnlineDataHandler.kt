package com.kalzakath.zoodle

import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class OnlineDataHandler(
    val onDataReturned: (ArrayList<RowEntryModel>) -> Unit
) {

    private lateinit var myRef: DatabaseReference
    private var user: FirebaseUser? = null

    fun onItemDismissed(moodEntry: MoodEntryModel) {
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
    fun read(user: FirebaseUser?): ArrayList<RowEntryModel> {
        val moodData = ArrayList<RowEntryModel>()

        myRef.child(user!!.uid).child("moodEntries").get()
            .addOnSuccessListener (){
                if (it.value?.javaClass == HashMap<String, Any>().javaClass) {
                    println("Value: " + it.value)
                    println("Children: " + it.childrenCount)
                    for ((key, hashmap) in it.value as HashMap<String, HashMap<String, Any>>) {
                        moodData.add(
                            MoodEntryModel(
                                hashmap["date"].toString(),
                                hashmap["time"].toString(),
                                Mood(hashmap["mood"] as HashMap<String, Any>),
                                if (hashmap["feelings"] == null) arrayListOf() else hashmap["feelings"] as MutableList<String>,
                                if (hashmap["activities"] == null) arrayListOf() else hashmap["activities"] as MutableList<String>,
                                key
                            )
                        )
                    }
                    onDataReturned(moodData)
                }
            }.addOnFailureListener {
                println("Unable to get data from DB")
            }
        println("Size: " + moodData.size)
        return moodData
    }

    private fun checkDatabasePathExists() {
        if (myRef.child(user?.uid ?: "").child("moodEntries") == null) myRef.child(user?.uid ?: "")
            .child("moodEntries").setValue("")
    }
}