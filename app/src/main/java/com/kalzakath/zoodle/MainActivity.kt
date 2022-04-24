package com.kalzakath.zoodle

import android.content.Intent
import android.content.SharedPreferences
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
import java.io.*
import java.security.SecureRandom
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
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
    private val SHARED_PREF_SECRET_KEY = "secretKey"

    private val isDebugMode = true
    private lateinit var settings: Settings

    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) { res ->
            this.onSignInResult(res)
        }

    private fun setupRecycleView() {
        recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry, moodList -> onItemDismissed(moodEntry, moodList) },
            { moodEntries -> writeEntryToFile(moodEntries); updateDatabaseEntry(moodEntries) },
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
        val jsonArray = createJSONFromFile("available.json")

        // Get activities that are stored in local json file
        if (jsonArray?.isNotEmpty() == true) {
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
        val data = recyclerViewAdaptor.getMoodList() as ArrayList<out Parcelable>
        if (data.isNotEmpty()) intent.putParcelableArrayListExtra("MoodEntries", data)

        getSettingsActivityResult.launch(intent)
    }

    private fun startActivityTrendView() {
        val intent = Intent(this, TrendViewActivity::class.java)

        intent.putExtra("Settings", settings)
        val data = recyclerViewAdaptor.getMoodList() as ArrayList<out Parcelable>
        if (data.isNotEmpty()) intent.putParcelableArrayListExtra("MoodEntries", data)

        getTrendViewActivitiesResult.launch(intent)
    }

    private fun startActivityFeelings(moodEntry: MoodEntryModel) {
        val intent = Intent(this, FeelingsActivity::class.java)
        val jsonArray = createJSONFromFile("feelings.json")

        // Get activities that are stored in local json file
        if (jsonArray?.isNotEmpty() == true) {
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
            value.toDouble().div(settings.mood_max!!.toInt() / 5)
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
        writeEntryToFile(moodList)
    }

    private fun setupMoodPicker(moodEntry: MoodEntryModel) {
        val numberPicker: NumberPicker = findViewById(R.id.npNumberPicker)
        val numberArray = Array(settings.mood_max!!.toInt()) { (it + 1).toString() }

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
            val moodValue: Mood = when (settings.mood_numerals) {
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

    private fun collectMoodData() {

        val moodData: ArrayList<MoodEntryModel> = if (user != null) {
            checkDatabasePathExists()
            readDatabaseForNewData()
        } else {
            readFromLocalStore()
        }
        /*for (i in 1..1000) {
            moodData.add(MoodEntryModel())
        }*/
        recyclerViewAdaptor.updateList(moodData)
    }

    private fun setActivityListeners() {
        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    writeEntryToFile(data as ArrayList<*>, "available.json")
                    recyclerViewAdaptor.updateMoodEntry(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                }
            }

        getTrendViewActivitiesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {

        }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                writeEntryToFile(data as ArrayList<*>, "feelings.json")
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
                    writeEntryToFile(data)
                    recyclerViewAdaptor.updateListConfig(data)
                    settings = data
                }

                recyclerViewAdaptor.updateList(moodData)
            }
    }

    private fun readSettingsDataFromJson(jsonSettings: String) {
        val gson = Gson()
        val type = object : TypeToken<Settings>() {}.type
        val data = gson.fromJson<Settings>(jsonSettings, type)
        if (data != null) settings = data
        else {
            settings = Settings()
            writeEntryToFile(settings)
        }
    }

    private fun generateSecretKey(): SecretKey {
        val secureRandom = SecureRandom()
        val keyGen = KeyGenerator.getInstance("AES")
        keyGen.init(256, secureRandom)
        return keyGen.generateKey()
    }

    private fun saveSecretKey(sharedPref: SharedPreferences, secretKey: SecretKey) {
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        sharedPref.edit().putString(SHARED_PREF_SECRET_KEY, encodedKey).apply()
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
        val jsonArray = createJSONFromFile()
        val moodData = ArrayList<MoodEntryModel>()

        if (jsonArray?.isNotEmpty() == true) {
            val gson = GsonBuilder().create()
            val type = object : TypeToken<Array<MoodEntryModel>>() {}.type
            val moodEntries = gson.fromJson<Array<MoodEntryModel>>(jsonArray, type)
            if (moodEntries.isEmpty()) return moodData

            for (x in moodEntries.indices) {
                when (settings.mood_numerals) {
                    "true" -> moodEntries[x].mood!!.moodMode = Mood.MOOD_MODE_NUMBERS
                    else -> moodEntries[x].mood!!.moodMode = Mood.MOOD_MODE_FACES
                }
                moodData.add(moodEntries[x])
            }
        }

        return moodData
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val jsonSettings = createJSONFromFile("settings.json")
        if (jsonSettings != null) readSettingsDataFromJson(jsonSettings)
        else settings = Settings()

        if(::settings.isInitialized)  setupRecycleView()

        if (isDebugMode) isPremiumEdition = true

        if (::recyclerViewAdaptor.isInitialized) {
            recyclerView.adapter = recyclerViewAdaptor

            user = null
            collectMoodData()
            initButtons()
            setActivityListeners()
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

    private fun getSecretKey(): SecretKey {
        val mPrefs = applicationContext.getSharedPreferences("Pref", MODE_PRIVATE)
        val key = mPrefs.getString(SHARED_PREF_SECRET_KEY, null)

        if (key == null) {
            val secretKey = generateSecretKey()
            saveSecretKey(mPrefs, secretKey)
            return secretKey
        }

        val decodeKey = Base64.getDecoder().decode(key)

        return SecretKeySpec(decodeKey, 0, decodeKey.size, "AES")
    }

    private fun encryptData(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val data = secretKey.encoded
        val sKeySpec = SecretKeySpec(data, 0, data.size, "AES")
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.ENCRYPT_MODE, sKeySpec, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    private fun writeEntryToFile(jsonString: String, filename: String) {
        val secretKey = getSecretKey()
        val fileData = jsonString.toByteArray(Charsets.UTF_32)
        val encoded = encryptData(secretKey, fileData)

        val fos: FileOutputStream = openFileOutput(filename, MODE_PRIVATE)
        val bos = BufferedOutputStream(fos)
        bos.write(encoded)
        bos.flush()
        bos.close()
    }

    private fun writeEntryToFile(data: ArrayList<*>, filename: String = "testData.json") {
        val gson = Gson()
        val jsonString: String = gson.toJson(data)
        writeEntryToFile(jsonString, filename)
    }

    private fun writeEntryToFile(data: Settings, filename: String = "settings.json") {
        val gson = Gson()
        val jsonString: String = gson.toJson(data)
        writeEntryToFile(jsonString, filename)
    }

    private fun readDataFromFile(filename: String): ByteArray? {
        val path = this.filesDir.absoluteFile

        val file = File("$path/$filename")
        if (file != null && file.isFile) {
            val fileContents = file.readBytes()
            val inputBuffer = BufferedInputStream(FileInputStream(file))
            inputBuffer.read(fileContents)
            inputBuffer.close()

            return fileContents
        }
        return null
    }

    private fun decrypt(secretKey: SecretKey, fileData: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES", "BC")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(cipher.blockSize)))
        return cipher.doFinal(fileData)
    }

    private fun createJSONFromFile(filename: String = "testData.json"): String? {
        val data = readDataFromFile(filename)
        if (data?.isNotEmpty() == true) {
            val secretKey = getSecretKey()
            val decryptedData = decrypt(secretKey, data)
            return decryptedData.toString(Charsets.UTF_32)
        }
        return null
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
