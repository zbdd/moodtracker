package com.example.rows

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        res -> this.onSignInResult(res)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // ...
            val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMain)
            val tvLoading: TextView = findViewById(R.id.tv_loading)
            recyclerView.layoutManager = LinearLayoutManager(this)
            val data = ArrayList<MoodEntryModel>()

            val database = Firebase.database("https://silent-blend-161710-default-rtdb.asia-southeast1.firebasedatabase.app")
            val myRef = database.reference
            if (user != null) {
                if (myRef.child(user.uid).child("moodEntries") == null) myRef.child(user.uid).child("moodEntries").setValue("")

                myRef.child(user.uid).child("moodEntries").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.value == null) return
                        data.clear()
                        if (snapshot.value!!.javaClass == HashMap<String, Any>().javaClass) {
                            for ((key, hashmap) in snapshot.value as HashMap<String, HashMap<String, String>>) {
                                data.add(
                                    MoodEntryModel(
                                        hashmap["date"].toString(),
                                        hashmap["mood"].toString(),
                                        hashmap["activity"].toString()
                                    )
                                )
                            }
                            val adaptor = RecyclerViewAdaptor(data)
                            recyclerView.adapter = adaptor

                            adaptor.run {
                                tvLoading.visibility = View.INVISIBLE
                                updateList()
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        tvLoading.visibility = View.INVISIBLE
                        TODO("Not yet implemented")
                    }
                })

                //loadTestingData(data, adaptor)

                val addNewButton: ImageButton = findViewById(R.id.addNewButton)
                addNewButton.setOnClickListener {
                    val moodEntry = createNewMoodEntry()
                    //data.clear()
                    data.add(moodEntry)
                    //writeEntrytoFile(moodEntry)
                    val moodHash = moodEntry.toMap()
                    val key = myRef.child(user.uid).child("moodEntries").push().key
                    val update = hashMapOf<String, Any>("moodEntries/$key" to moodHash)
                    myRef.child(user.uid).updateChildren(update)

                    val adaptor = RecyclerViewAdaptor(data)
                    recyclerView.adapter = adaptor
                    adaptor.run {
                        updateList()
                    }
                }

            }
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            // ...
            launchSignInEvent()
        }
    }

    fun loadTestingData(data: ArrayList<MoodEntryModel>, adaptor: RecyclerViewAdaptor) {
        val jsonArray = loadFromJSONAsset()

        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val moodEntries = gson.fromJson(jsonArray, Array<MoodEntryModel>::class.java).toList()
            for(x in moodEntries.indices) {
                data.add(moodEntries[x])
            }
        }
        adaptor.run {
            notifyDataSetChanged()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launchSignInEvent()
    }

    private fun launchSignInEvent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    fun writeEntrytoFile(moodEntryModel: MoodEntryModel) {
        var gson = Gson()
        var jsonString: String = gson.toJson(moodEntryModel)
        val fileout: FileOutputStream = openFileOutput("testData.json", MODE_PRIVATE)
        val outwrite: OutputStreamWriter = OutputStreamWriter(fileout)
        outwrite.append(jsonString)
        outwrite.close()
        fileout.close()
    }

    fun loadFromJSONAsset(): String {
        var json = ""

        println("File to read: " + this.filesDir.absoluteFile)

        val file = File("testData.json")
        if (file.isFile) {
            val fileReader: FileReader = FileReader("testData.json")
            json = fileReader.readLines().toString()

        }
        else {
            val inputStream = assets.open("testData.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = Charsets.UTF_8
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, charset)
        }

        return json
    }

    fun createNewMoodEntry(): MoodEntryModel {

        //val date = LocalDateTime.now()
        //val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        //val formatted = date.format(formatter)
        var randMonth = kotlin.random.Random.nextInt(1,12).toString()
        if (randMonth.toInt() < 10) randMonth = "0$randMonth"
        var randDay = kotlin.random.Random.nextInt(1,28).toString()
        if (randDay.toInt() < 10) randDay = "0$randDay"
        val randMood = kotlin.random.Random.nextInt(1,9).toString()

        return MoodEntryModel("2022-$randMonth-$randDay", "$randMood", "Test Data New")
    }
}