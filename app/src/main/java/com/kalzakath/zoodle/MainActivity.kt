package com.kalzakath.zoodle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Toast
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
import com.kalzakath.zoodle.debug.DebugDataHandler
import java.lang.reflect.Modifier
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {
    private lateinit var myRef: DatabaseReference
    private lateinit var getActivitiesActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getSettingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getFeelingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getTrendViewActivitiesResult: ActivityResultLauncher<Intent>
    private lateinit var secureFileHandler: SecureFileHandler
    private lateinit var dataHandler: DataHandler

    private var user: FirebaseUser? = null
    private var isPremiumEdition = false
    private val debug = object {
        fun debugDataHandler(boolean: Boolean) {
            dataHandler = if (boolean) DebugDataHandler(secureFileHandler, applicationContext)
            else DataHandler(secureFileHandler, applicationContext)
        }
    }

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secureFileHandler = SecureFileHandler(applicationContext)
        val settingsString = secureFileHandler.read("settings.json")
        readSettingsDataFromJson(settingsString)

        val recyclerViewAdaptor = setupRecycleView()

        user = null
        dataHandler = DataHandler(secureFileHandler, applicationContext)
        initButtons(recyclerViewAdaptor)
        setActivityListeners(recyclerViewAdaptor)

        //debug.debugDataHandler(true)
        recyclerViewAdaptor.updateList(dataHandler.getMoodData())
        recyclerViewAdaptor.updateListConfig()
    }

    private fun setupRecycleView(): RecyclerViewAdaptor {
        val recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodList -> secureFileHandler.write(moodList); updateDatabaseEntry(moodList) },
            { moodEntry, recycleViewAdaptor -> setupMoodPicker(moodEntry, recycleViewAdaptor) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) })

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(recyclerViewAdaptor)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdaptor

        return recyclerViewAdaptor
    }

    private fun startActivityActivities(moodEntry: MoodEntryModel) {
        val intent = Intent(this, ActivitiesActivity::class.java)
        val jsonArray = secureFileHandler.read("available.json")

        // Get activities that are stored in local json file
        if (jsonArray?.isNotEmpty() == true) {
            val gson = GsonBuilder().create()
            val activities = gson.fromJson(jsonArray, ArrayList::class.java)
            if (activities.isNotEmpty()) {
                val data = activities.filterIsInstance<String>() as ArrayList<String>
                intent.putStringArrayListExtra("AvailableActivities", data)
            }
        }

        intent.putExtra("MoodEntry", moodEntry)
        getActivitiesActivityResult.launch(intent)
    }

    private fun startActivitySettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        getSettingsActivityResult.launch(intent)
    }

    private fun startActivityTrendView() {
        val intent = Intent(this, TrendViewActivity::class.java)
        getTrendViewActivitiesResult.launch(intent)
    }

    private fun startActivityFeelings(moodEntry: MoodEntryModel) {
        val intent = Intent(this, FeelingsActivity::class.java)
        val jsonArray = secureFileHandler.read("feelings.json")

        // Get activities that are stored in local json file
        if (jsonArray?.isNotEmpty() == true) {
            val gson = GsonBuilder().create()
            val feelings = gson.fromJson(jsonArray, ArrayList::class.java)
            var data = ArrayList<String>()
            if (feelings.isNotEmpty()) data = feelings.filterIsInstance<String>() as ArrayList<String>
            intent.putStringArrayListExtra("AvailableFeelings", data)
        }

        intent.putExtra("MoodEntry", moodEntry)

        getFeelingsActivityResult.launch(intent)
    }

    private fun onItemDismissed(moodEntry: MoodEntryModel, moodList: ArrayList<MoodEntryModel>) {
        if (user != null) myRef.child(user?.uid ?: "").child("moodEntries")
            .child(moodEntry.key).removeValue()
        secureFileHandler.write(moodList)
    }

    private fun setupMoodPicker(moodEntry: MoodEntryModel, recyclerViewAdaptor: RecyclerViewAdaptor) {
        val numberPicker: NumberPicker = findViewById(R.id.npNumberPicker)
        val numberArray = Array(Settings.moodMax) { (it + 1).toString() }
        val mvHelper = MoodValueHelper()

        when (moodEntry.mood!!.moodMode) {
            Mood.MOOD_MODE_NUMBERS -> {
                numberPicker.displayedValues = numberArray
                numberPicker.maxValue = Settings.moodMax.minus(1)
                numberPicker.minValue = 0
                numberPicker.wrapSelectorWheel = true
                numberPicker.textColor = Color.WHITE
                numberPicker.value = moodEntry.mood.value!!.toInt().minus(1)

            }
            Mood.MOOD_MODE_FACES -> {
                numberPicker.displayedValues = resources.getStringArray(R.array.mood_faces)
                numberPicker.minValue = 0
                numberPicker.maxValue = resources.getStringArray(R.array.mood_faces).size - 1
                numberPicker.value =
                    mvHelper.getSanitisedNumber(moodEntry.mood.value!!.toInt(), Settings.moodMax).minus(1)
            }
        }

        val clNumberPicker: ConstraintLayout = findViewById(R.id.clNumberPicker)
        clNumberPicker.visibility = View.VISIBLE

        val bNpConfirm: Button = findViewById(R.id.bNpConfirm)

        bNpConfirm.setOnClickListener {
            val moodValue: Mood = when (moodEntry.mood.moodMode) {
                Mood.MOOD_MODE_NUMBERS -> Mood((numberPicker.value + 1).toString(), Mood.MOOD_MODE_NUMBERS)
                else -> Mood(mvHelper.getUnsanitisedNumber(numberPicker.value + 1, Settings.moodMax).toString(), Mood.MOOD_MODE_FACES)
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

    private fun setActivityListeners(recyclerViewAdaptor: RecyclerViewAdaptor) {
        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    secureFileHandler.write(data as ArrayList<*>, "available.json")
                    recyclerViewAdaptor.updateMoodEntry(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                }
            }

        getTrendViewActivitiesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                secureFileHandler.write(data as ArrayList<*>, "feelings.json")
                recyclerViewAdaptor.updateMoodEntry(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
            }
        }

        getSettingsActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                //val data = it.data?.getParcelableExtra<Settings>("Settings")
                val moodEntries = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                var moodData = ArrayList<MoodEntryModel>()
                if (moodEntries != null) {
                    val data = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                    if (data != null) moodData = data.filterIsInstance<MoodEntryModel>() as ArrayList<MoodEntryModel>
                }

                secureFileHandler.write(Settings)
                recyclerViewAdaptor.updateListConfig()

                recyclerViewAdaptor.updateList(moodData)
            }
    }

    private fun createMoodPicker(recyclerViewAdaptor: RecyclerViewAdaptor) {
        val moodPicker = MoodPickerDialog(this) { moodEntry -> addNewMoodEntry(moodEntry, recyclerViewAdaptor) }
        moodPicker.showPopup()
    }

    private fun readSettingsDataFromJson(jsonSettings: String?) {
        val gson = GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT).create()
        val type = object : TypeToken<Settings>() {}.type
        val data = gson.fromJson<Settings>(jsonSettings, type)
        if (data != null) {
            Settings.moodMode = data.moodMode
            Settings.moodMax = data.moodMax
        }
        else {
            secureFileHandler.write(Settings)
        }
    }

    private fun initButtons(recyclerViewAdaptor: RecyclerViewAdaptor) {
        val addNewButton: ImageButton = findViewById(R.id.addNewButton)

        addNewButton.setOnClickListener {
            //addNewMoodEntry(false)
            createMoodPicker(recyclerViewAdaptor)
        }

        val bViewTrend: Button = findViewById(R.id.bViewTrend)
        bViewTrend.setOnClickListener {
            startActivityTrendView()
        }

        val ibSettings: ImageButton = findViewById(R.id.ibSettings)
        ibSettings.setOnClickListener {
            startActivitySettings()
        }

        //if (isDebugMode) {
            val ibAddNewDebug: ImageButton = findViewById(R.id.ibAddNewDebug)
            ibAddNewDebug.setOnClickListener {
                addNewMoodEntry(recyclerViewAdaptor)
            }
       // }

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

    private fun addNewMoodEntry(recyclerViewAdaptor: RecyclerViewAdaptor) {
        addNewMoodEntry(dataHandler.createNewMoodEntry(LocalDateTime.now()), recyclerViewAdaptor)
    }

    private fun addNewMoodEntry(moodEntry: MoodEntryModel, recyclerViewAdaptor: RecyclerViewAdaptor) {
        val data: ArrayList<MoodEntryModel> = ArrayList()
        data.add(moodEntry)
        recyclerViewAdaptor.updateList(data)
    }

    @Suppress("UNCHECKED_CAST")
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
}
