package com.iodaniel.notesio

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.iodaniel.notesio.databinding.ActivityMainBinding
import com.iodaniel.notesio.databinding.BottomsheetHomeModalContentBinding
import com.iodaniel.notesio.note_package.FragmentNotes
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.task_card_package.FragmentTaskCards
import com.iodaniel.notesio.view_model_package.ViewModelTaskCards
import kotlinx.coroutines.*
import java.util.*

class MainActivity : AppCompatActivity(), OnClickListener,
    ConfigurationListener, CustomCLickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var configurationListener: ConfigurationListener
    private val homeModalBottomSheet = HomeModalBottomSheet()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>
    private lateinit var taskCardDao: TaskCardDatabase
    private var taskCardData: ArrayList<TaskCardData> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurationListener = this
        supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes()).commit()
        if (binding.activityMainLandNav == null) configurationListener.smallWidth()
        else if (binding.activityMainLandNav != null) configurationListener.largeWidth()
    }

    private fun bottomSheetControls() {
        if (bottomSheetBehavior != null && !homeModalBottomSheet.isAdded) {

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
                BottomSheetBehavior.STATE_DRAGGING -> {

                }
                BottomSheetBehavior.STATE_SETTLING -> {

                }
            }
        }
    }

    private fun openNotes() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            delay(500)
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes())
                .commit()
        }
        homeModalBottomSheet.dismiss()
    }

    private fun openTasks() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            delay(500)
            supportFragmentManager.beginTransaction()
                .addToBackStack("task cards")
                .replace(R.id.home_root, FragmentTaskCards())
                .commit()
        }
        homeModalBottomSheet.dismiss()
    }

    override fun largeWidth() {
        binding.activityMainNotes!!.setOnClickListener(this)
        binding.activityMainTasks!!.setOnClickListener(this)
    }

    override fun smallWidth() {
        setSupportActionBar(binding.homeToolbar)
        binding.activityMainBottomTab!!.setOnClickListener(this)
        bottomSheetBehavior = BottomSheetBehavior.from(binding.dialogBottomSheet!!)
        homeModalBottomSheet.customCLickListener = this
    }

    override fun fragmentNoteClicked() {
        openNotes()
    }

    override fun fragmentTaskClicked() {
        openTasks()
    }

    override fun onBackPressed() {
        super.onBackPressed()
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
            R.id.activity_main_bottom_tab -> {
                bottomSheetControls()
            }
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
        if (customCLickListener != null) {
            when (v?.id) {
                R.id.home_modal_note -> customCLickListener.fragmentNoteClicked()
                R.id.home_modal_tasks -> customCLickListener.fragmentTaskClicked()
                R.id.bottom_sheet_optional_login -> {
                    startActivity(Intent(requireContext(), SignInSignUp::class.java))
                    requireActivity().overridePendingTransition(0, 0)
                }
            }
        }
    }

    override fun signedIn() {
        binding.bottomSheetOptionalLogin.visibility = View.GONE
    }

    override fun signedOut() {
        binding.bottomSheetOptionalLogin.setOnClickListener(this)
        binding.bottomSheetOptionalLogin.visibility = View.VISIBLE
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