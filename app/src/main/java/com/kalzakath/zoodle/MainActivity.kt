package com.kalzakath.zoodle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
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
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.ceil
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerViewAdaptor: RecyclerViewAdaptor
    private lateinit var recyclerView: RecyclerView
    private lateinit var myRef: DatabaseReference
    private lateinit var getActivitiesActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getSettingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getFeelingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getTrendViewActivitiesResult: ActivityResultLauncher<Intent>

    private var user: FirebaseUser? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var isPremiumEdition = false

    private val isDebugMode = true
    lateinit private var settings: Settings

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }

    private fun setupRecycleView() {
        recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntrytoFile(moodEntries); updateDatabaseEntry(moodEntries) },
            { mood -> setupMoodPicker(mood) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            settings)

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(recyclerViewAdaptor)
        mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper?.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        recyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val tvLoading: TextView = findViewById(R.id.tv_loading)
        tvLoading.visibility = View.INVISIBLE
    }

    private fun startActivityActivities(moodEntry: MoodEntryModel) {
        val intent = Intent(this, ActivitiesActivity::class.java)
        val jsonArray = loadFromJSONAsset("available.json")

        // Get activities that are stored in local json file
        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val activities = gson.fromJson(jsonArray, ArrayList::class.java)
            if (activities.isNotEmpty()) {
                val data = activities[0] as ArrayList<String>
                intent.putStringArrayListExtra("AvailableActivities", data)
            }
        }

        intent.putExtra("MoodEntry", moodEntry)

        getActivitiesActivityResult.launch(intent)
    }

    private fun startActivitySettings() {
        val intent = Intent(this, SettingsActivity::class.java)

        intent.putExtra("Settings", settings)
        val data = recyclerViewAdaptor.getMoodList() as java.util.ArrayList<out Parcelable>
        if (data.isNotEmpty()) intent.putParcelableArrayListExtra("MoodEntries", data)

        getSettingsActivityResult.launch(intent)
    }

    private fun startActivityTrendView() {
        val intent = Intent(this, TrendViewActivity::class.java)

        intent.putExtra("Settings", settings)
        val data = recyclerViewAdaptor.getMoodList() as java.util.ArrayList<out Parcelable>
        if (data.isNotEmpty()) intent.putParcelableArrayListExtra("MoodEntries", data)

        getTrendViewActivitiesResult.launch(intent)
    }

    private fun startActivityFeelings(moodEntry: MoodEntryModel) {
        val intent = Intent(this, FeelingsActivity::class.java)
        val jsonArray = loadFromJSONAsset("feelings.json")

        // Get activities that are stored in local json file
        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val feelings = gson.fromJson(jsonArray, ArrayList::class.java)
            var data = ArrayList<String>()
            if (feelings.isNotEmpty()) data = feelings[0] as ArrayList<String>
            intent.putStringArrayListExtra("AvailableFeelings", data)
        }

        intent.putExtra("MoodEntry", moodEntry)

        getFeelingsActivityResult.launch(intent)
    }

    private fun getSanitisedNumber(value: Int): Int {
        return (ceil(
            value?.toDouble()?.div(settings.mood_max!!.toInt() / 5) as Double
        ).toInt())
    }

    private fun getUnsanitisedNumber(value: Int): Int {
        return value.times(settings.mood_max!!.toInt()/ 5)
    }


    private fun getEmoji(convertValue: String): Int {
        return when (convertValue) {
            "Ecstatic" -> R.string.mood_ecstatic
            "Happy" -> R.string.mood_happy
            "Unhappy" -> R.string.mood_unhappy
            "Terrible" -> R.string.mood_terrible
            else -> R.string.mood_average
        }
    }

    private fun onItemDismissed(moodEntry: MoodEntryModel, moodList: ArrayList<MoodEntryModel>) {
        if (user != null) myRef.child(user?.uid ?: "").child("moodEntries")
            .child("${moodEntry.key}").removeValue()
        writeEntrytoFile(moodList)
    }

    private fun setupMoodPicker(moodEntry: MoodEntryModel) {
        val numberPicker: NumberPicker = findViewById(R.id.npNumberPicker)
        val numberArray = Array(settings!!.mood_max!!.toInt()) { (it + 1).toString() }

        when (settings.mood_numerals) {
            "true" -> {
                numberPicker.displayedValues = numberArray
                numberPicker.maxValue = settings.mood_max?.toInt()?.minus(1)?: 4
                numberPicker.minValue = 0
                numberPicker.wrapSelectorWheel = true
                numberPicker.textColor = Color.WHITE
                numberPicker.value = moodEntry.mood!!.value!!.toInt().minus(1)

            }
            "false" -> {
                numberPicker.displayedValues = resources.getStringArray(R.array.mood_faces)
                numberPicker.minValue = 0
                numberPicker.maxValue = resources.getStringArray(R.array.mood_faces).size - 1
                numberPicker.value =
                    getSanitisedNumber(moodEntry.mood!!.value!!.toInt()).minus(1)
            }
        }

        val clNumberPicker: ConstraintLayout = findViewById(R.id.clNumberPicker)
        clNumberPicker.visibility = View.VISIBLE

        val bNpConfirm: Button = findViewById(R.id.bNpConfirm)

        bNpConfirm.setOnClickListener {
            var moodValue: Mood = when (settings.mood_numerals) {
                "true" -> Mood((numberPicker.value + 1).toString(), Mood.MOOD_MODE_NUMBERS)
                else -> Mood(getUnsanitisedNumber(numberPicker.value + 1).toString(), Mood.MOOD_MODE_FACES)
            }
            val newMood = MoodEntryModel(
                moodEntry.date,
                moodEntry.time,
                moodValue,
                moodEntry.feelings,
                moodEntry.activities,
                moodEntry.key,
                LocalDateTime.now().toString()
            )
            clNumberPicker.visibility = View.INVISIBLE
            recyclerViewAdaptor.updateMoodEntry(newMood)
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        user = FirebaseAuth.getInstance().currentUser
        val ibLogin: ImageButton = findViewById(R.id.ibLogin)

        if (result.resultCode == RESULT_OK) {
            val database =
                Firebase.database("https://silent-blend-161710-default-rtdb.asia-southeast1.firebasedatabase.app")
            myRef = database.reference

            checkDatabasePathExists()
            readDatabaseForNewData()

            ibLogin.background.setTint(Color.GREEN)
        } else {
            user = null
            ibLogin.background.setTint(Color.LTGRAY)
            Toast.makeText(applicationContext, "Unable to sign-in at this time", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun runMainLoop() {

        var moodData = ArrayList<MoodEntryModel>()
        /* Please ignore this horror
        var stringArray = ArrayList<String>()
        stringArray.add("Boxing")
        stringArray.add("Dancing")
        stringArray.add("Gaming")

        writeEntrytoFile(stringArray,"available.json")
         */

        //settings.mood_numerals = "false"
        //writeEntrytoFile(settings, "settings.json")

        initButtons()

        if (user != null) {
            checkDatabasePathExists()
            moodData = readDatabaseForNewData()
        } else {
            moodData = readFromLocalStore()
        }

        recyclerViewAdaptor.updateList(moodData)

        val ibLogin: ImageButton = findViewById(R.id.ibLogin)
        if (user == null) ibLogin.background.setTint(Color.LTGRAY)
        else ibLogin.background.setTint(Color.GREEN)

        ibLogin.setOnClickListener {
            if (!isPremiumEdition) {
                Toast.makeText(
                    applicationContext,
                    "Premium edition feature only",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (user == null) {
                launchSignInEvent()
            } else {
                Toast.makeText(applicationContext, "Already signed in", Toast.LENGTH_SHORT).show()
            }
        }

        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    writeEntrytoFile(data as ArrayList<*>, "available.json")
                    recyclerViewAdaptor.updateMoodEntry(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                }
            }

        getTrendViewActivitiesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                writeEntrytoFile(data as ArrayList<*>, "feelings.json")
                recyclerViewAdaptor.updateMoodEntry(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
            }
        }

        getSettingsActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getParcelableExtra<Settings>("Settings")
                val moodEntries = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                var moodData = ArrayList<MoodEntryModel>()
                if (moodEntries != null) moodData = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries") as ArrayList<MoodEntryModel>

                if (data != null) {
                    writeEntrytoFile(data, "settings.json")
                    recyclerViewAdaptor.updateListConfig(data)
                    settings = data
                }

                recyclerViewAdaptor.updateList(moodData)
            }
    }

    private fun readSettingsDataFromJson(jsonSettings: String) {
        val gson = Gson()
        val type = object : TypeToken<Array<Settings>>() {}.type
        val data = gson.fromJson<Array<Settings>>(jsonSettings, type)
        if (data.isNotEmpty()) settings = data[0]
        else {
            settings = Settings()
            writeEntrytoFile(settings, "settings.json")
        }
    }

    private fun initButtons() {
        val addNewButton: ImageButton = findViewById(R.id.addNewButton)

        addNewButton.setOnClickListener {
            addNewMoodEntry(false)
        }

        val bViewTrend: Button = findViewById(R.id.bViewTrend)
        bViewTrend.setOnClickListener {
            startActivityTrendView()
        }

        val ibSettings: ImageButton = findViewById(R.id.ibSettings)
        ibSettings.setOnClickListener {
            startActivitySettings()
        }

        if (isDebugMode) {
            val ibAddNewDebug: ImageButton = findViewById(R.id.ibAddNewDebug)
            ibAddNewDebug.setOnClickListener {
                addNewMoodEntry(isDebugMode)
            }
        }
    }

    private fun updateDatabaseEntry(moods: ArrayList<MoodEntryModel>) {
        if (user != null) {
            for (moodEntry in moods) {
                val moodHash = moodEntry.toMap()
                val update = hashMapOf<String, Any>("moodEntries/${moodEntry.key}" to moodHash)
                myRef.child(user?.uid ?: "").updateChildren(update)
            }
        }
    }

    private fun addNewMoodEntry(isDebug: Boolean) {
        val moodEntry = createNewMoodEntry(isDebug, LocalDateTime.now())

        val data: ArrayList<MoodEntryModel> = ArrayList()
        data.add(moodEntry)
        recyclerViewAdaptor.updateList(data)
    }

    private fun readDatabaseForNewData(): ArrayList<MoodEntryModel> {
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

    private fun readFromLocalStore(): ArrayList<MoodEntryModel> {
        val jsonArray = loadFromJSONAsset()
        val moodData = ArrayList<MoodEntryModel>()

        if (jsonArray.isNotEmpty()) {
            val gson = GsonBuilder().create()
            val type = object : TypeToken<Array<Array<MoodEntryModel>>>() {}.type
            val moodEntries = gson.fromJson<Array<Array<MoodEntryModel>>>(jsonArray, type)
            if (moodEntries.isEmpty()) return moodData

            for (x in moodEntries[0].indices) {
                when (settings.mood_numerals) {
                    "true" -> moodEntries[0][x].mood!!.moodMode = Mood.MOOD_MODE_NUMBERS
                    else -> moodEntries[0][x].mood!!.moodMode = Mood.MOOD_MODE_FACES
                }
                moodData.add(moodEntries[0][x])
            }
        }

        return moodData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonSettings = loadFromJSONAsset("settings.json")
        readSettingsDataFromJson(jsonSettings)

        if(::settings.isInitialized)  setupRecycleView()

        if (isDebugMode) isPremiumEdition = true

        if (::recyclerViewAdaptor.isInitialized) {
            recyclerView.adapter = recyclerViewAdaptor

            user = null
            runMainLoop()
        }
    }

    private fun launchSignInEvent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setIsSmartLockEnabled(true)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun writeEntrytoFile(data: ArrayList<*>, filename: String = "testData.json") {
        val gson = Gson()
        val jsonString: String = gson.toJson(data)
        val fileout: FileOutputStream = openFileOutput(filename, MODE_PRIVATE)
        val outwrite = OutputStreamWriter(fileout)
        outwrite.write(jsonString)
        outwrite.close()
        fileout.close()
    }

    private fun writeEntrytoFile(data: Settings, filename: String = "") {
        if (filename == "") return

        val gson = Gson()
        val jsonString: String = gson.toJson(data)
        val fileout: FileOutputStream = openFileOutput(filename, MODE_PRIVATE)
        val outwrite = OutputStreamWriter(fileout)
        outwrite.write(jsonString)
        outwrite.close()
        fileout.close()
    }

    private fun loadFromJSONAsset(filename: String = "testData.json"): String {
        val json: String

        val path = this.filesDir.absoluteFile

        val file = File("$path/$filename")
        if (!file.isFile) file.createNewFile()

        if (file.isFile) {
            val fileReader = FileReader("$path/$filename")
            json = fileReader.readLines().toString()
            fileReader.close()
            return json
        } else {
            val inputStream = assets.open(filename)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            val charset: Charset = Charsets.UTF_8
            inputStream.read(buffer)
            inputStream.close()
            json = String(buffer, charset)
            return json
        }
    }

    private fun createNewMoodEntry(isDebug: Boolean, dateTimeNow: LocalDateTime): MoodEntryModel {
        val random = Random(System.currentTimeMillis())

        val choices: MutableList<String> = ArrayList()
        choices.add("Programming")
        choices.add("Gaming")
        choices.add("Reading")
        choices.add("Going out")
        choices.add("School")
        choices.add("Rugby")
        choices.add("DnD")
        choices.add("Hanging out")

        val availFeelings = resources.getStringArray(R.array.available_feelings)

        val list: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            list.add(choices[random.nextInt(0, choices.size - 1)])
        }

        val feelings: MutableList<String> = ArrayList()
        for (i in 1..random.nextInt(4)) {
            feelings.add(availFeelings[random.nextInt(0, availFeelings.size - 1)])
        }

        return if (isDebug) {
            val randomYear = random.nextInt(2010, 2021).toString()
            var randMonth = random.nextInt(1, 12).toString()
            if (randMonth.toInt() < 10) randMonth = "0$randMonth"
            var randDay = random.nextInt(1, 28).toString()
            if (randDay.toInt() < 10) randDay = "0$randDay"
            val randMood = random.nextInt(1, 5).toString()

            when (settings.mood_numerals) {
                "true" -> MoodEntryModel(
                    "$randomYear-$randMonth-$randDay",
                    "12:34",
                    Mood(randMood),
                    feelings,
                    list,
                    UUID.randomUUID().toString()
                )
                else -> {
                    MoodEntryModel(
                        "$randomYear-$randMonth-$randDay",
                        "12:34",
                        Mood("3", Mood.MOOD_MODE_FACES),
                        feelings,
                        list,
                        UUID.randomUUID().toString()
                    )
                }
            }
        } else {
            var dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = dateTimeNow.format(dateTimeFormatter)

            dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")
            val time = dateTimeNow.format(dateTimeFormatter)

            when (settings.mood_numerals) {
                "true" -> MoodEntryModel(date, time, Mood("3"), feelings, list, UUID.randomUUID().toString())
                else -> MoodEntryModel(
                    date,
                    time,
                    Mood("3", Mood.MOOD_MODE_FACES),
                    ArrayList(),
                    ArrayList(),
                    UUID.randomUUID().toString()
                )
            }
        }
    }
}
