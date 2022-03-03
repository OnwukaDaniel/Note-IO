package com.iodaniel.notesio

import android.graphics.Point
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import com.google.android.material.navigation.NavigationView.OnNavigationItemSelectedListener
import com.iodaniel.notesio.databinding.ActivityMainBinding
import com.iodaniel.notesio.note_package.FragmentNotes
import com.iodaniel.notesio.task_card_package.FragmentTaskCards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), OnClickListener, OnNavigationItemSelectedListener,
    ConfigurationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var configurationListener: ConfigurationListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.homeToolbar)
        configurationListener = this
        supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes()).commit()
        binding.activityMainTitle.text = "Notes"

        if (binding.activityMainLandNav == null) {
            configurationListener.smallWidth()
        } else if (binding.activityMainLandNav != null) {
            configurationListener.largeWidth()
        }

        val display = windowManager.defaultDisplay
        val point = Point()
        display.getSize(point)
        println("THIS IS THE SCREEN ******************** height: ${point.y} ---- width: ${point.x}")
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.home_menu_icon -> binding.homeDrawer.openDrawer(GravityCompat.START)
            R.id.activity_main_notes -> openNotes()
            R.id.activity_main_tasks -> openTasks()
        }
    }

    private fun openNotes() {
        val scope = CoroutineScope(Dispatchers.IO)
        if (binding.activityMainLandNav == null) binding.homeDrawer.closeDrawer(GravityCompat.START)
        scope.launch {
            delay(500)
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes())
                .commit()
        }
        binding.activityMainTitle.text = "Notes"
    }

    private fun openTasks() {
        val scope = CoroutineScope(Dispatchers.IO)
        if (binding.activityMainLandNav == null) binding.homeDrawer.closeDrawer(GravityCompat.START)
        scope.launch {
            delay(500)
            supportFragmentManager.beginTransaction()
                .addToBackStack("task cards")
                .replace(R.id.home_root, FragmentTaskCards())
                .commit()
        }
        binding.activityMainTitle.text = "Tasks"
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home_nav_notes -> {
                openNotes()
                return true
            }
            R.id.home_nav_tasks -> {
                openTasks()
                return true
            }
        }
        return false
    }

    override fun largeWidth() {
        binding.activityMainNotes!!.setOnClickListener(this)
        binding.activityMainTasks!!.setOnClickListener(this)
    }

    override fun smallWidth() {
        binding.homeMenuIcon!!.setOnClickListener(this)
        binding.homeNav!!.setNavigationItemSelectedListener(this)
        if (binding.homeDrawer.isDrawerOpen(GravityCompat.START)) binding.homeDrawer.closeDrawer(
            GravityCompat.END
        )
    }
}

interface ConfigurationListener {
    fun largeWidth()
    fun smallWidth()
}