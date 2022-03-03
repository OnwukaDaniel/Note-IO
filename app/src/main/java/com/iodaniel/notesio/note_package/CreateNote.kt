package com.iodaniel.notesio.note_package

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.iodaniel.notesio.MainActivity
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.ActivityCreateNoteBinding
import com.iodaniel.notesio.databinding.CreateTitleDialogBinding
import com.iodaniel.notesio.room_package.HistoryNote
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.room_package.NoteDatabase
import com.iodaniel.notesio.view_model_package.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateNote : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityCreateNoteBinding
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var homeViewModel: HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createNoteToolbar)
        title = ""
        binding.createTitle.setOnClickListener(this)
        if (intent.hasExtra("note data")) {
            val json = intent.getStringExtra("note data")
            val noteData: NoteData = Gson().fromJson(json, NoteData::class.java)
            setData(noteData)
        }
        homeViewModel = HomeViewModel()
        supportActionBar!!.setHomeButtonEnabled(true)
    }

    private fun setData(noteData: NoteData) {
        binding.createTitle.text = noteData.noteTitle
        binding.createNote.setText(noteData.note)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(applicationContext).inflate(R.menu.create_note_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_menu_save -> saveNote()
            R.id.create_menu_delete -> deleteNote()
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun deleteNote() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            if (intent.hasExtra("note data")) {
                noteDatabase = NoteDatabase.getDatabaseInstance(applicationContext)!!
                val json = intent.getStringExtra("note data")
                val noteData: NoteData = Gson().fromJson(json, NoteData::class.java)
                noteDatabase.noteDao().deleteNote(noteData)
                noteDatabase = NoteDatabase.getDatabaseInstance(applicationContext)!!
                val intent = Intent(this@CreateNote, MainActivity::class.java)
                runOnUiThread{ onBackPressed() }
            }
        }
    }

    private fun saveNote() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val datetime = Calendar.getInstance().time.time

            noteDatabase = NoteDatabase.getDatabaseInstance(applicationContext)!!
            val newIntent = Intent(this@CreateNote, MainActivity::class.java)
            newIntent.flags = /*Intent.FLAG_ACTIVITY_NEW_TASK and */Intent.FLAG_ACTIVITY_CLEAR_TASK

            if (intent.hasExtra("note data")) {
                val json = intent.getStringExtra("note data")
                val noteData: NoteData = Gson().fromJson(json, NoteData::class.java)
                val history = HistoryNote()
                history.noteTitle = noteData.noteTitle
                history.note = noteData.note
                history.dateCreated = noteData.dateCreated
                history.dateModified = datetime.toString()

                noteData.modifiedHistory.add(history)
                noteData.note = binding.createNote.text!!.trim().toString()
                noteData.noteTitle = binding.createTitle.text.trim().toString()
                noteDatabase.noteDao().updateNote(noteData)
                runOnUiThread{ onBackPressed() }
                return@launch
            } else if (!intent.hasExtra("note data")) {
                var title = binding.createTitle.text!!.trim().toString()
                val note = binding.createNote.text!!.trim().toString()
                if (title == "") title = "Untitled"
                if (note == "") return@launch

                val noteData = NoteData()
                noteData.noteTitle = title
                noteData.dateCreated = datetime.toString()
                noteData.note = note
                noteDatabase.noteDao().insertNote(noteData)
                runOnUiThread{ onBackPressed() }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.create_title -> {
                val dialog = Dialog(this)
                val bindingDialog = CreateTitleDialogBinding.inflate(dialog.layoutInflater)
                dialog.setContentView(bindingDialog.root)
                val height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.window?.setLayout(resources.displayMetrics.widthPixels, height)
                dialog.setTitle("Note title")
                bindingDialog.createDialogTitle.setText(binding.createTitle.text.toString())
                dialog.show()
                dialog.setOnDismissListener {
                    binding.createTitle.text =
                        bindingDialog.createDialogTitle.text!!.trim().toString()
                }
                dialog.show()
            }
        }
    }
}