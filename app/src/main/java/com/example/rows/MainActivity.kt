package com.example.rows

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdaptor: RecyclerViewAdaptor
    private lateinit var recyclerView: RecyclerView
    private lateinit var myRef: DatabaseReference
    private var user: FirebaseUser? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var isOnlineEnabled = false
    private var isPremiumEdition = false

    private val isDebugMode = true

    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
        res -> this.onSignInResult(res)
    }

    fun setupRecycleView() {
        recyclerViewAdaptor = RecyclerViewAdaptor (
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntrytoFile(moodEntries) })

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(recyclerViewAdaptor)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val tvLoading: TextView = findViewById(R.id.tv_loading)
        tvLoading.visibility = View.INVISIBLE
    }

    private fun onItemDismissed(moodEntry: MoodEntryModel, moodList: ArrayList<MoodEntryModel>) {
        if (user != null && myRef != null) myRef.child(user!!.uid).child("moodEntries").child("${moodEntry.key}").removeValue()
        writeEntrytoFile(moodList)
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

        initButtons()

        val ibLogin: ImageButton = findViewById(R.id.ibLogin)
        if (!isOnlineEnabled) ibLogin.background.setTint(Color.LTGRAY)
        else ibLogin.background.setTint(Color.GREEN)

        ibLogin.setOnClickListener {
            if (!isPremiumEdition) {
                Toast.makeText(applicationContext,"Premium edition feature only", Toast.LENGTH_SHORT).show()
            }
            else if(!isOnlineEnabled) { isOnlineEnabled = true; launchSignInEvent() }
            else { Toast.makeText(applicationContext,"Already signed in", Toast.LENGTH_SHORT).show() }
        }
    }

    private fun initButtons () {
        val addNewButton: ImageButton = findViewById(R.id.addNewButton)

        addNewButton.setOnClickListener {
            addNewMoodEntry(false)
        }

        if (isDebugMode) {
            val ibAddNewDebug: ImageButton = findViewById(R.id.ibAddNewDebug)
            ibAddNewDebug.setOnClickListener {
                addNewMoodEntry(isDebugMode)
            }
        }
    }

    private fun addNewMoodEntry(isDebug: Boolean) {
        val moodEntry = createNewMoodEntry(isDebug)

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
                                hashmap["time"].toString(),
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

        if (isDebugMode) isPremiumEdition = true

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
            .setIsSmartLockEnabled(true)
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

    private fun createNewMoodEntry(isDebug: Boolean): MoodEntryModel {
        return if (isDebug) {
            var randMonth = kotlin.random.Random.nextInt(1,12).toString()
            if (randMonth.toInt() < 10) randMonth = "0$randMonth"
            var randDay = kotlin.random.Random.nextInt(1,28).toString()
            if (randDay.toInt() < 10) randDay = "0$randDay"
            val randMood = kotlin.random.Random.nextInt(1,9).toString()

            MoodEntryModel("2022-$randMonth-$randDay", "12:34", randMood, "Test Data New")
        } else {
            var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val dateTimeNow = LocalDateTime.now()
            val date = dateTimeNow.format(dateTimeFormatter)

            dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val time = dateTimeNow.format(dateTimeFormatter)

            MoodEntryModel(date, time,"5","")
        }
    }
}
