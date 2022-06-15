package com.kalzakath.zoodle

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseUser
import com.google.gson.GsonBuilder
import com.kalzakath.zoodle.debug.TestSuite
import com.kalzakath.zoodle.interfaces.DataController
import com.kalzakath.zoodle.interfaces.MainActivityInterface
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.logging.Logger

class MainActivity : AppCompatActivity(), MainActivityInterface {

    private lateinit var getActivitiesActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getSettingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getFeelingsActivityResult: ActivityResultLauncher<Intent>
    private lateinit var getTrendViewActivitiesResult: ActivityResultLauncher<Intent>
    private lateinit var getFrontPageActivityResult: ActivityResultLauncher<Intent>
    private lateinit var rowController: DataController
    private lateinit var secureFileHandler: SecureFileHandler
    private lateinit var onlineDataHandler: FirebaseConnectionHandler
    private lateinit var dataHandler: DataHandler
    private val log = Logger.getLogger(MainActivity::class.java.name + "****************************************")

    private var user: FirebaseUser? = null
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            this.onSignInResult(it)
        }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        user = onlineDataHandler.onSignInResult(result)
        val loginBtn: ImageButton = findViewById(R.id.mainibLogin)

        if (user == null) {
            Toast.makeText(applicationContext, "Unable to sign-in at this time", Toast.LENGTH_SHORT)
                .show()
            loginBtn.background = AppCompatResources.getDrawable(applicationContext, R.drawable.main_login_gray)
        }
        else {
            loginBtn.background = AppCompatResources.getDrawable(applicationContext, R.drawable.main_login_green)
            Toast.makeText(applicationContext, "Retrieving online information...", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        secureFileHandler = SecureFileHandler(applicationContext)
        rowController = RowController()
        onlineDataHandler = FirebaseConnectionHandler()
        val moodTrackerMain = MoodTrackerMain(secureFileHandler,rowController,onlineDataHandler)

        //dataHandler = DataHandler(secureFileHandler, applicationContext)

        setupRecycleView()
        initButtons()
        setActivityListeners()

        //dataHandler = TestSuite.useLocalData(secureFileHandler, applicationContext)
        TestSuite.setDefaultSettings()

        //rowController.update(dataHandler.read())

        val moodEntry = intent.getSerializableExtra("MoodEntry")
        if (moodEntry != null) rowController.update(moodEntry as MoodEntryModel)

        val todayMoodEntry: RowEntryModel? = rowController.find("Date", DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(LocalDate.now()))
        if (todayMoodEntry == null) startActivityFrontPage(null)
    }

    override fun setupRecycleView(): RecyclerViewAdaptor {
        val recyclerViewAdaptor = RecyclerViewAdaptor(
            { moodEntry -> createMoodValuePicker(moodEntry) },
            { moodEntry -> startActivityActivities(moodEntry) },
            { moodEntry -> startActivityFeelings(moodEntry) },
            rowController)

        recyclerViewAdaptor.onLongPress = {
            log.info("Consumed onLongPress")
            startActivityFrontPage(it)
        }

        val callback: ItemTouchHelper.Callback = SwipeHelperCallback(recyclerViewAdaptor)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(findViewById(R.id.recyclerViewMain))

        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewMain)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = recyclerViewAdaptor

        return recyclerViewAdaptor
    }

    override fun startActivityActivities(moodEntry: MoodEntryModel) {
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

    override fun startActivitySettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        getSettingsActivityResult.launch(intent)
    }

    override fun startActivityFrontPage(moodEntry: MoodEntryModel?) {
        val intent = Intent(this, FrontPageActivity::class.java)
        if (moodEntry != null) intent.putExtra("MoodEntry", moodEntry)
        getFrontPageActivityResult.launch(intent)
    }

    override fun startActivityTrendView() {
        val intent = Intent(this, TrendViewActivity::class.java)
        getTrendViewActivitiesResult.launch(intent)
    }

    override fun startActivityFeelings(moodEntry: MoodEntryModel) {
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

    override fun createMoodValuePicker(moodEntry: MoodEntryModel) {
        val numberPicker: NumberPicker = findViewById(R.id.npNumberPicker)
        val numberArray = Array(Settings.moodMax) { (it + 1).toString() }
        val mvHelper = MoodValueHelper()

        when (Settings.moodMode) {
            Settings.MoodModes.NUMBERS -> {
                numberPicker.displayedValues = numberArray
                numberPicker.maxValue = Settings.moodMax.minus(1)
                numberPicker.minValue = 0
                numberPicker.wrapSelectorWheel = true
                numberPicker.textColor = Color.WHITE
                numberPicker.value = moodEntry.mood?.value!!.toInt().minus(1)

            }
            else -> {
                numberPicker.displayedValues = resources.getStringArray(R.array.mood_faces)
                numberPicker.minValue = 0
                numberPicker.maxValue = resources.getStringArray(R.array.mood_faces).size - 1
                numberPicker.value =
                    mvHelper.getSanitisedNumber(moodEntry.mood?.value!!.toInt(), Settings.moodMax).minus(1)
            }
        }

        val clNumberPicker: ConstraintLayout = findViewById(R.id.clNumberPicker)
        clNumberPicker.visibility = View.VISIBLE

        val bNpConfirm: Button = findViewById(R.id.bNpConfirm)

        bNpConfirm.setOnClickListener {
            val moodValue: Mood = when (Settings.moodMode) {
                Settings.MoodModes.NUMBERS -> Mood((numberPicker.value + 1).toString())
                else -> Mood(mvHelper.getUnsanitisedNumber(numberPicker.value + 1, Settings.moodMax).toString())
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
            rowController.update(newMood)
        }
    }

    private fun setActivityListeners() {
        getActivitiesActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                val data = it.data?.getStringArrayListExtra("AvailableActivities")
                if (data != null) {
                    secureFileHandler.write(data as ArrayList<*>, "available.json")
                    rowController.update(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
                }
            }

        getTrendViewActivitiesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        }

        getFrontPageActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel
            rowController.update(data)
        }

        getFeelingsActivityResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            val data = it.data?.getStringArrayListExtra("AvailableFeelings")
            if (data != null) {
                secureFileHandler.write(data as ArrayList<*>, "feelings.json")
                rowController.update(it.data?.getSerializableExtra("MoodEntry") as MoodEntryModel)
            }
        }

        getSettingsActivityResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
                //val data = it.data?.getParcelableExtra<Settings>("Settings")
                val moodEntries = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                var moodData = ArrayList<RowEntryModel>()
                if (moodEntries != null) {
                    val data = it.data?.getParcelableArrayListExtra<Parcelable>("MoodEntries")
                    if (data != null) moodData = data.filterIsInstance<RowEntryModel>() as ArrayList<RowEntryModel>
                }

                secureFileHandler.write(Settings)
//                recyclerViewAdaptor.updateListConfig()

                rowController.update(moodData)
            }
    }

    override fun createMoodEntryPicker() {
        val moodPicker = MoodEntryPicker(this) { moodEntry -> rowController.add(moodEntry) }
        moodPicker.showPopup()
    }

    private fun initButtons() {
        val addNewButton: ImageButton = findViewById(R.id.addNewButton)

        addNewButton.setOnClickListener {
            createMoodEntryPicker()
        }

        val bViewTrend: Button = findViewById(R.id.bViewTrend)
        bViewTrend.setOnClickListener {
            startActivityTrendView()
        }

        val ibSettings: ImageButton = findViewById(R.id.ibSettings)
        ibSettings.setOnClickListener {
            startActivitySettings()
        }

        val ibAddNewDebug: ImageButton = findViewById(R.id.ibAddNewDebug)
        ibAddNewDebug.setOnClickListener {
            val debugMood = MoodEntryFactory().createDebug(applicationContext)
            rowController.add(debugMood)
        }

        val ibLogin: ImageButton = findViewById(R.id.mainibLogin)
        if (user == null) ibLogin.background.setTint(Color.LTGRAY)
        else ibLogin.background.setTint(Color.GREEN)

        ibLogin.setOnClickListener {
            if (!Settings.isPremiumEdition) {
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

    override fun launchSignInEvent() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setLogo(R.mipmap.ic_alfielauncher_foreground)
            .setIsSmartLockEnabled(false)
            .setAvailableProviders(providers)
            .build()
        signInLauncher.launch(signInIntent)
    }
}
