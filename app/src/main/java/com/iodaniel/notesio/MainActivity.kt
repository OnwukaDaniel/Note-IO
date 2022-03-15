package com.iodaniel.notesio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.iodaniel.notesio.databinding.ActivityMainBinding
import com.iodaniel.notesio.databinding.BottomsheetHomeModalContentBinding
import com.iodaniel.notesio.extra_classes.ClosingActivity
import com.iodaniel.notesio.extra_classes.SettingsActivity
import com.iodaniel.notesio.extra_classes.SignInSignUp
import com.iodaniel.notesio.note_package.FragmentNotes
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.task_card_package.FragmentTaskCards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnClickListener,
    ConfigurationListener, CustomCLickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var configurationListener: ConfigurationListener
    private val homeModalBottomSheet = HomeModalBottomSheet()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var taskCardDao: TaskCardDatabase
    private var taskCardData: ArrayList<TaskCardData> = arrayListOf()
    private val fragmentIds: ArrayList<String> = arrayListOf("NOTES", "TASKS")
    private var fragmentId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurationListener = this
        supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes()).commit()
        fragmentId = fragmentIds[0]

        if (binding.activityMainLandNav == null) configurationListener.smallWidth()
        else if (binding.activityMainLandNav != null) configurationListener.largeWidth()
    }

    private fun bottomSheetControls() {
        if (!homeModalBottomSheet.isAdded) {
            try {
                when (bottomSheetBehavior.state) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        homeModalBottomSheet.show(supportFragmentManager, "MODAL BOTTOM SHEET")
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                    }
                    BottomSheetBehavior.STATE_HIDDEN -> {
                        homeModalBottomSheet.show(supportFragmentManager, "MODAL BOTTOM SHEET")
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun openNotes() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            when (fragmentId) {
                "NOTES" -> {
                }
                "TASKS" -> {
                    supportFragmentManager.popBackStack(
                        null,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.home_root, FragmentNotes())
                        .commit()
                    fragmentId = fragmentIds[0]
                }
            }
        }
    }

    private fun openTasks() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            when (fragmentId) {
                "NOTES" -> {
                    supportFragmentManager.beginTransaction()
                        .addToBackStack("task cards")
                        .replace(R.id.home_root, FragmentTaskCards())
                        .commit()
                    fragmentId = fragmentIds[1]
                }
                "TASKS" -> {
                }
            }
        }
    }

    override fun largeWidth() {
        binding.activityMainNotes!!.setOnClickListener(this)
        binding.activityMainTasks!!.setOnClickListener(this)
        binding.activityMainAccountLarge!!.setOnClickListener(this)
        binding.activityMainSettingLarge!!.setOnClickListener(this)
    }

    override fun smallWidth() {
        setSupportActionBar(binding.homeToolbar)
        binding.activityMainBottomTab!!.setOnClickListener(this)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.dialogBottomSheet!!)
        homeModalBottomSheet.customCLickListener = this
    }

    override fun fragmentNoteClicked() {
        openNotes()
        homeModalBottomSheet.dismiss()
    }

    override fun fragmentTaskClicked() {
        openTasks()
        homeModalBottomSheet.dismiss()
    }

    override fun onBackPressed() {
        val intent = Intent(applicationContext, ClosingActivity::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        overridePendingTransition(0, 0)
        finish()
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.activity_main_notes -> openNotes()
            R.id.activity_main_tasks -> openTasks()
            R.id.activity_main_setting_large -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                overridePendingTransition(0, 0)
            }
            R.id.activity_main_account_large -> {
                startActivity(Intent(applicationContext, SignInSignUp::class.java))
                overridePendingTransition(0, 0)
            }
            R.id.activity_main_bottom_tab -> bottomSheetControls()
        }
    }
}

interface ConfigurationListener {
    fun largeWidth()
    fun smallWidth()
}

class HomeModalBottomSheet : BottomSheetDialogFragment(), OnClickListener, SignedInListener {

    private lateinit var binding: BottomsheetHomeModalContentBinding
    lateinit var customCLickListener: CustomCLickListener
    private lateinit var signedInListener: SignedInListener
    private val auth = FirebaseAuth.getInstance().currentUser

    override fun onStart() {
        super.onStart()
        binding.homeModalNote.setOnClickListener(this)
        binding.homeModalTasks.setOnClickListener(this)
        binding.homeModalSettings.setOnClickListener(this)
        binding.homeModalAccount.setOnClickListener(this)
        signedInListener = this
        when (auth) {
            null -> signedInListener.signedOut()
            else -> signedInListener.signedIn()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomsheetHomeModalContentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onClick(v: View?) {
        try {
            when (v?.id) {
                R.id.home_modal_note -> customCLickListener.fragmentNoteClicked()
                R.id.home_modal_tasks -> customCLickListener.fragmentTaskClicked()
                R.id.home_modal_settings -> {
                    startActivity(Intent(requireContext(), SettingsActivity::class.java))
                    requireActivity().overridePendingTransition(0, 0)
                    dismiss()
                }
                R.id.home_modal_account -> {
                    startActivity(Intent(requireContext(), SignInSignUp::class.java))
                    requireActivity().overridePendingTransition(0, 0)
                    dismiss()
                }
            }
        } catch (e: Exception) {
        }
    }

    override fun signedIn() {
        binding.homeModalAccount.visibility = View.GONE
    }

    override fun signedOut() {
        binding.homeModalAccount.setOnClickListener(this)
        binding.homeModalAccount.visibility = View.VISIBLE
    }
}

interface CustomCLickListener {
    fun fragmentNoteClicked()
    fun fragmentTaskClicked()
}

interface SignedInListener {
    fun signedIn()
    fun signedOut()
}