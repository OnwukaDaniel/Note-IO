package com.iodaniel.notesio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.iodaniel.notesio.databinding.ActivityMainBinding
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.utils.HomeViewModel

class MainActivity : AppCompatActivity(), View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.homeToolbar)
        binding.homeMenuIcon.setOnClickListener(this)
        binding.homeNav.setNavigationItemSelectedListener(this)
        supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes()).commit()
        if (binding.homeDrawer.isDrawerOpen(GravityCompat.START)) binding.homeDrawer.closeDrawer(GravityCompat.END)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.home_menu_icon -> binding.homeDrawer.openDrawer(GravityCompat.START)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.menu.home_nav_menu -> {
                supportFragmentManager.beginTransaction().replace(R.id.home_root, FragmentNotes()).commit()
                binding.homeDrawer.closeDrawer(GravityCompat.END)
                return true
            }
        }
        return false
    }
}