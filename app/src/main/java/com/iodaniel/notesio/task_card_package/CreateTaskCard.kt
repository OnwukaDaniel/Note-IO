package com.iodaniel.notesio.task_card_package

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.CreateTitleDialogBinding
import com.iodaniel.notesio.databinding.FragmentCreateTaskCardBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CreateTaskCard : AppCompatActivity(), OnClickListener {
    private lateinit var binding: FragmentCreateTaskCardBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCreateTaskCardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.taskCardTitle.setOnClickListener(this)
        binding.cardSave.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        val taskCardDao = TaskCardDatabase.getDatabaseInstance(applicationContext)!!
        val scope = CoroutineScope(Dispatchers.IO)
        when (v?.id) {
            R.id.task_card_title -> {
                val dialog = Dialog(this)
                val bindingDialog = CreateTitleDialogBinding.inflate(dialog.layoutInflater)
                dialog.setContentView(bindingDialog.root)
                val height = WindowManager.LayoutParams.WRAP_CONTENT
                dialog.window?.setLayout(resources.displayMetrics.widthPixels, height)
                bindingDialog.createDialogTitle.setText(binding.taskCardTitle.text.toString())
                dialog.show()
                dialog.setOnDismissListener {
                    binding.taskCardTitle.text =
                        bindingDialog.createDialogTitle.text!!.trim().toString()
                }
                dialog.show()
            }
            R.id.card_save -> {
                scope.launch {
                    val taskCardData = TaskCardData()
                    val title = binding.taskCardTitle.text.trim().toString()
                    if (title == "") return@launch
                    taskCardData.cardTitle = title
                    taskCardData.dateCreated = Calendar.getInstance().time.time.toString()
                    taskCardDao.taskDao().insertTaskCard(taskCardData)
                    runOnUiThread { onBackPressed() }
                }
            }
        }
    }
}
