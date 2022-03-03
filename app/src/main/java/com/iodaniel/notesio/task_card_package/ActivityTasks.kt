package com.iodaniel.notesio.task_card_package

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.ActivityTasksBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.room_package2.TaskData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityTasks : AppCompatActivity(), View.OnClickListener, TaskAvailabilityListener {
    private lateinit var binding: ActivityTasksBinding
    private lateinit var taskCardData: TaskCardData
    private lateinit var taskAvailabilityListener: TaskAvailabilityListener
    private var tasAdapter = TaskAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        title = ""
        setSupportActionBar(binding.activityTaskToolbar)

        binding.tasksFab.setOnClickListener(this)
        taskAvailabilityListener = this
        val json = intent!!.getStringExtra("data")
        taskCardData = Gson().fromJson(json, TaskCardData::class.java)

        binding.activityTaskTitle.text = taskCardData.cardTitle
        tasAdapter.taskCardData = taskCardData
        tasAdapter.dataset = taskCardData.taskData
        binding.tasksRv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.tasksRv.adapter = tasAdapter
        if (taskCardData.taskData.isEmpty()) taskAvailabilityListener.noTask() else taskAvailabilityListener.taskPresent()

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(applicationContext).inflate(R.menu.task_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val scope = CoroutineScope(Dispatchers.IO)
        when (item.itemId) {
            R.id.delete_tasks_card -> {
                if (taskCardData != null) {
                    scope.launch {
                        val taskCardDao = TaskCardDatabase.getDatabaseInstance(applicationContext)
                        taskCardDao!!.taskDao().deleteNote(taskCardData)
                        runOnUiThread { onBackPressed() }
                        return@launch
                    }
                }
                return true
            }
            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tasks_fab -> {
                val json = Gson().toJson(taskCardData)
                val intent = Intent(applicationContext, ActivityCreateTask::class.java)
                intent.putExtra("data", json)
                startActivity(intent)
                overridePendingTransition(0, 0)
            }
        }
    }

    override fun noTask() {
        binding.taskAvailabilityRoot.visibility = View.VISIBLE
    }

    override fun taskPresent() {
        binding.taskAvailabilityRoot.visibility = View.GONE
    }
}

interface TaskAvailabilityListener {
    fun noTask()
    fun taskPresent()
}

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.ViewHolder>() {
    lateinit var taskCardData: TaskCardData
    lateinit var dataset: ArrayList<TaskData>
    private lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.task_row_title)
        val note: TextView = itemView.findViewById(R.id.task_row_note)
        val deadline: TextView = itemView.findViewById(R.id.task_row_deadline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.tasks_row, parent, false)
        return ViewHolder((view))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.title.text = taskCardData.cardTitle
        holder.note.text = datum.note
        holder.deadline.text = datum.deadline
    }

    override fun getItemCount() = dataset.size
}