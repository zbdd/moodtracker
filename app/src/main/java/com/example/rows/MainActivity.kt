package com.example.rows

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdaptor: RecyclerViewAdaptor
    private lateinit var recyclerView: RecyclerView
    private lateinit var myRef: DatabaseReference
    private var user: FirebaseUser? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private val isOnlineEnabled = false

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        res -> this.onSignInResult(res)
    }

    fun setupRecycleView() {
        recyclerViewAdaptor = RecyclerViewAdaptor (
            { moodEntry -> onItemDismissed(moodEntry) },
            { moodEntries -> writeEntrytoFile(moodEntries) })

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(recyclerViewAdaptor)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val tvLoading: TextView = findViewById(R.id.tv_loading)
        tvLoading.visibility = View.INVISIBLE
    }

    private fun onItemDismissed(moodEntry: MoodEntryModel) {
        myRef.child(user!!.uid).child("moodEntries").child("${moodEntry.key}").removeValue()
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        user = FirebaseAuth.getInstance().currentUser

        if (result.resultCode == RESULT_OK) {
            val database =
                Firebase.database("https://silent-blend-161710-default-rtdb.asia-southeast1.firebasedatabase.app")
            myRef = database.reference
        }

        runMainLoop()
    }

    private fun runMainLoop() {
        if (user != null) {
            checkDatabasePathExists()
            readDatabaseForNewData()
        } else {
            readFromLocalStore()
        }

        addDataOnClick()
    }

    private fun addDataOnClick () {
        val addNewButton: ImageButton = findViewById(R.id.addNewButton)
        addNewButton.setOnClickListener {
            val moodEntry = createNewMoodEntry()

            if (user != null) {
                val moodHash = moodEntry.toMap()
                val key = myRef.child(user!!.uid).child("moodEntries").push().key
                val update = hashMapOf<String, Any>("moodEntries/$key" to moodHash)
                myRef.child(user!!.uid).updateChildren(update)
            } else {
                var data: ArrayList<MoodEntryModel> = ArrayList()
                data.add(moodEntry)
                recyclerViewAdaptor.run {
                    updateList(data)
                }
            }
        }
    }

    private fun readDatabaseForNewData() {
        val data = ArrayList<MoodEntryModel>()

        myRef.child(user!!.uid).child("moodEntries").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.value == null) return

                if (snapshot.value!!.javaClass == HashMap<String, Any>().javaClass) {
                    for ((key, hashmap) in snapshot.value as HashMap<String, HashMap<String, String>>) {
                        data.add(
                            MoodEntryModel(
                                hashmap["date"].toString(),
                                hashmap["mood"].toString(),
                                hashmap["activity"].toString(),
                                key
                            )
                        )
                    }

                    recyclerViewAdaptor.run {
                        updateList(data)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Debug","Failed to connect to database")
            }
        })
    }

    private fun checkDatabasePathExists() {
        if (myRef.child(user!!.uid).child("moodEntries") == null) myRef.child(user!!.uid).child("moodEntries").setValue("")
    }

    private fun readFromLocalStore() {
        val jsonArray = loadFromJSONAsset()
        val data = ArrayList<MoodEntryModel>()

        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object: TypeToken<Array<Array<MoodEntryModel>>>() {}.type
            val moodEntries = gson.fromJson<Array<Array<MoodEntryModel>>>(jsonArray, type)
            for(x in moodEntries[0].indices) {
                data.add(moodEntries[0][x])
            }
        }
        recyclerViewAdaptor.run {
            updateList(data)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupRecycleView()

        Log.d("Debug", "Launching app...")

        if (::recyclerViewAdaptor.isInitialized) {
            recyclerView.adapter = recyclerViewAdaptor

            if (isOnlineEnabled) {
                launchSignInEvent()
            } else {
                user = null
                runMainLoop()
            }
        }
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

    fun writeEntrytoFile(data: ArrayList<MoodEntryModel>) {
        val gson = Gson()
        val jsonString: String = gson.toJson(data)
        val fileout: FileOutputStream = openFileOutput("testData.json", MODE_PRIVATE)
        val outwrite = OutputStreamWriter(fileout)
        outwrite.write(jsonString)
        outwrite.close()
        fileout.close()
    }

    private fun loadFromJSONAsset(): String {
        var json = ""

        println("File to read: " + this.filesDir.absoluteFile)
        val path = this.filesDir.absoluteFile

        val file = File("$path/testData.json")
        if (file.isFile) {
            val fileReader = FileReader("$path/testData.json")
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

    private fun createNewMoodEntry(): MoodEntryModel {
        var randMonth = kotlin.random.Random.nextInt(1,12).toString()
        if (randMonth.toInt() < 10) randMonth = "0$randMonth"
        var randDay = kotlin.random.Random.nextInt(1,28).toString()
        if (randDay.toInt() < 10) randDay = "0$randDay"
        val randMood = kotlin.random.Random.nextInt(1,9).toString()

        return MoodEntryModel("2022-$randMonth-$randDay", randMood, "Test Data New")
    }
}
