package com.iodaniel.notesio.extra_classes

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.widget.CompoundButton
import android.widget.CompoundButton.OnCheckedChangeListener
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.ActivitySettingsBinding
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.view_model_package.HomeViewModel
import com.iodaniel.notesio.view_model_package.ViewModelTaskCards
import kotlinx.coroutines.*

class SettingsActivity : AppCompatActivity(), OnClickListener, OnCheckedChangeListener {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var settingsPref: SharedPreferences
    private var homeViewModel = HomeViewModel()
    private var viewModelTaskCards = ViewModelTaskCards()
    private val auth = FirebaseAuth.getInstance().currentUser
    private var backUpNoteRef = FirebaseDatabase.getInstance().reference
    private var backUpTaskRef = FirebaseDatabase.getInstance().reference
    private var noteData: List<NoteData> = listOf()
    private var taskCardData: List<TaskCardData> = listOf()
    private var notify: Boolean = false
    private var backUp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        settingsPref = getSharedPreferences(getString(R.string.SETTINGS_SHAREDPREFERENCE), Context.MODE_PRIVATE)

        homeViewModel.getAllData(applicationContext)!!
            .observe(this@SettingsActivity, { noteDataList ->
                noteData = noteDataList
            })
        viewModelTaskCards.getAllTaskCards(applicationContext)
            .observe(this@SettingsActivity, { taskCardDataList ->
                taskCardData = taskCardDataList
                runOnUiThread {
                    Snackbar.make(binding.root, "Back up complete", Snackbar.LENGTH_SHORT)
                        .show()
                }
            })
        backUpNoteRef = backUpNoteRef.child(auth!!.uid).child("data_back_up").child("note").push()
        backUpTaskRef = backUpTaskRef.child(auth.uid).child("data_back_up").child("task").push()

        loadSharedPreferences()

        binding.settingsBackUp.setOnCheckedChangeListener(this)
        binding.settingsNotification.setOnCheckedChangeListener(this)
        binding.settingsAccount.setOnClickListener(this)
        binding.settingsBackUp.setOnClickListener(this)
        binding.settingsTheme.setOnClickListener(this)
        binding.settingsWallpaper.setOnClickListener(this)
    }

    private fun loadSharedPreferences() {
        notify = settingsPref.getBoolean(getString(R.string.NOTIFICATION_SHAREDPREFERENCE), false)
        backUp = settingsPref.getBoolean(getString(R.string.BACKUP_SHAREDPREFERENCE), false)

        binding.settingsNotification.isChecked = notify
        binding.settingsBackUp.isChecked = backUp
    }

    private fun backUpData() {
        val view = layoutInflater.inflate(R.layout.progress_bar, null, false)
        val alertDialog = AlertDialog.Builder(this@SettingsActivity)
            .setView(view)
            .setCancelable(false)
            .show()

        if (auth != null) {
            backUpNoteRef.setValue(noteData).addOnCompleteListener {
                runOnUiThread { alertDialog.dismiss() }
            }.addOnFailureListener {
                runOnUiThread {
                    alertDialog.dismiss()
                    Snackbar.make(binding.root, it.localizedMessage, Snackbar.LENGTH_SHORT).show()
                }
                Snackbar.make(binding.root, it.localizedMessage, Snackbar.LENGTH_SHORT).show()
                return@addOnFailureListener
            }

            backUpTaskRef.setValue(taskCardData).addOnCompleteListener {
                runOnUiThread { alertDialog.dismiss() }
            }.addOnFailureListener {
                runOnUiThread {
                    alertDialog.dismiss()
                    Snackbar.make(binding.root, it.localizedMessage, Snackbar.LENGTH_SHORT).show()
                }
                return@addOnFailureListener
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.settings_account -> {
                startActivity(Intent(this, AccountSettings::class.java))
                overridePendingTransition(0, 0)
            }
            R.id.settings_theme -> {}
            R.id.settings_wallpaper -> {}

            R.id.settings_back_up -> {
            }
        }
    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        val switch = if (isChecked) "On" else "Off"
        when (buttonView!!.id) {
            R.id.settings_notification -> {
                settingsPref.edit().putBoolean("sharedPreferencesNotification", isChecked)
                    .apply()
                val notificationText = "Notification turned $switch"
                Snackbar.make(binding.root, notificationText, Snackbar.LENGTH_LONG).show()
            }

            R.id.settings_back_up -> {
                when (auth) {
                    null -> {
                        val snackBar =
                            Snackbar.make(binding.root, "You are not sign In", Snackbar.LENGTH_LONG)
                        snackBar.setAction("Sign In") {
                            startActivity(Intent(applicationContext, SignInSignUp::class.java))
                            overridePendingTransition(0, 0)
                        }.show()
                    }
                    else -> {
                        settingsPref.edit().putBoolean("sharedPreferencesBackUp", isChecked)
                            .apply()
                        val notificationText = "Back up turned $switch"
                        Snackbar.make(binding.root, notificationText, Snackbar.LENGTH_LONG)
                            .show()
                        if (isChecked) backUpData()
                    }
                }
            }
        }
    }
}