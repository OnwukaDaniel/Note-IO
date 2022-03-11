package com.iodaniel.notesio.task_card_package

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.ActivityTasksBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.room_package2.TaskData
import com.iodaniel.notesio.utils.Util
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ActivityTasks : AppCompatActivity(), OnClickListener, TaskAvailabilityListener,
    DimensionListener {
    private lateinit var binding: ActivityTasksBinding
    private lateinit var taskCardData: TaskCardData
    private lateinit var taskAvailabilityListener: TaskAvailabilityListener
    private lateinit var dimensionListener: DimensionListener
    private var taskAdapter = TaskAdapter()
    private var taskTabAdapter = TaskTabAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater, null, false)
        setContentView(binding.root)
        taskAvailabilityListener = this
        binding.tasksFab.setOnClickListener(this)
        dimensionListener = this

        val json = intent!!.getStringExtra("data")
        taskCardData = Gson().fromJson(json, TaskCardData::class.java)

        when (binding.activityTasksTabView) {
            null -> dimensionListener.smallScreen()
            else -> dimensionListener.largeScreen()
        }

        taskAdapter.taskCardData = taskCardData
        taskAdapter.dataset = taskCardData.taskData
        taskAdapter.activity = this
        binding.tasksRv.layoutManager =
            StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding.tasksRv.adapter = taskAdapter
        if (taskCardData.taskData.isEmpty()) taskAvailabilityListener.noTask() else taskAvailabilityListener.taskPresent()
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
            android.R.id.home -> onBackPressed()
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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun noTask() {
        binding.taskAvailabilityRoot.visibility = View.VISIBLE
    }

    override fun taskPresent() {
        binding.taskAvailabilityRoot.visibility = View.GONE
    }

    override fun largeScreen() {
        binding.activityTaskTabTitle!!.text = taskCardData.cardTitle
        binding.activityTaskDateCreated!!.text =  Util.convertLongToDate(taskCardData.dateCreated.toLong())

        taskTabAdapter.activity = this
        taskTabAdapter.dataset = taskCardData.taskData
        taskTabAdapter.taskCardData = taskCardData
        binding.activityTasksTabRv!!.adapter = taskTabAdapter
        binding.activityTasksTabRv!!.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
    }

    override fun smallScreen() {
        setSupportActionBar(binding.activityTaskToolbar)
        title = ""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        binding.activityTaskTitle!!.text = taskCardData.cardTitle
    }
}

interface TaskAvailabilityListener {
    fun noTask()
    fun taskPresent()
}

interface DimensionListener {
    fun largeScreen()
    fun smallScreen()
}

class TaskTabAdapter : RecyclerView.Adapter<TaskTabAdapter.TaskTabViewHolder>() {
    lateinit var taskCardData: TaskCardData
    lateinit var dataset: ArrayList<TaskData>
    private lateinit var context: Context
    lateinit var activity: Activity

    class TaskTabViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val deadline: TextView = itemView.findViewById(R.id.task_row_deadline_tab)
        val cardColor: LinearLayout = itemView.findViewById(R.id.task_row_tab_root)
        val note: TextView = itemView.findViewById(R.id.task_row_note_tab)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskTabViewHolder {
        context = parent.context
        val viewTab = LayoutInflater.from(context).inflate(R.layout.tasks_row_tab, parent, false)
        return TaskTabViewHolder(viewTab)
    }

    override fun onBindViewHolder(holder: TaskTabViewHolder, position: Int) {
        val datum = dataset[position]
        holder.deadline.text = Util.convertLongToDate(datum.deadline.toLong())
        holder.note.text = datum.note
        holder.cardColor.setBackgroundColor(datum.color)
        if (datum.color == Color.BLACK) {
            holder.note.setTextColor(Color.WHITE)
            holder.deadline.setTextColor(Color.WHITE)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityCreateTask::class.java)
            val json = Gson().toJson(taskCardData)
            val taskJson = Gson().toJson(datum)

            intent.putExtra("data", json)
            intent.putExtra("taskData", taskJson)
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount() = dataset.size
}

class TaskAdapter : RecyclerView.Adapter<TaskAdapter.PortraitViewHolder>() {
    lateinit var taskCardData: TaskCardData
    lateinit var dataset: ArrayList<TaskData>
    private lateinit var context: Context
    lateinit var activity: Activity

    class PortraitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val note: TextView = itemView.findViewById(R.id.task_row_note)
        val deadline: TextView = itemView.findViewById(R.id.task_row_deadline)
        val cardColor: ShapeableImageView = itemView.findViewById(R.id.task_row_label)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PortraitViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.tasks_row, parent, false)
        return PortraitViewHolder(view)
    }

    override fun onBindViewHolder(holder: PortraitViewHolder, position: Int) {
        val datum = dataset[position]
        holder.note.text = datum.note
        holder.deadline.text = Util.convertLongToDate(datum.deadline.toLong())
        holder.cardColor.setBackgroundColor(datum.color)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, ActivityCreateTask::class.java)
            val json = Gson().toJson(taskCardData)
            val taskJson = Gson().toJson(datum)

            intent.putExtra("data", json)
            intent.putExtra("taskData", taskJson)
            intent.putExtra("taskDataPosition", position)
            context.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount() = dataset.size
}
