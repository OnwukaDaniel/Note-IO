package com.iodaniel.notesio.task_card_package

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.ActivityCreateTaskBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.room_package2.TaskData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ActivityCreateTask : AppCompatActivity(), OnClickListener, DatePickerListener,
    TimePickerListener {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var datePickerListener: DatePickerListener
    private lateinit var timePickerListener: TimePickerListener
    private var dataset: ArrayList<Int> = arrayListOf(
        R.color.yellow,
        R.color.green,
        R.color.red,
        R.color.blue,
        R.color.gray,
        R.color.orange,
        R.color.pink
    )
    private val months = arrayListOf(
        "January", "February", "March", "April", "May", "June", "July",
        "August", "September", "October", "November", "December"
    )
    private lateinit var taskCardData: TaskCardData
    private var startTime = ""
    private var endTime = ""
    private var startDay = ""
    private var endDay = ""

    private val startTimePickerDialog = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val amPm = if (hourOfDay > 12) "PM" else "AM"
        val hour = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
        startTime = "$hour:$minute:$amPm"
        binding.createTaskStartTime.text = startTime
    }

    private val endTimePickerDialog = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        val amPm = if (hourOfDay > 12) "PM" else "AM"
        val hour = if (hourOfDay > 12) hourOfDay - 12 else hourOfDay
        endTime = "$hour:$minute:$amPm"
        binding.createTaskEndTime.text = endTime
    }
    private val startDatePickerDialog =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            startDay = "$dayOfMonth, ${months[month]}, $year"
            binding.createTaskStartDate.text = startDay
        }
    private val endDatePickerDialog =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            endDay = "$dayOfMonth, ${months[month]}, $year"
            binding.createTaskEndDate.text = endDay
        }

    private var labelAdapter = LabelAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.createTaskToolbar)

        if (intent.hasExtra("data")) {
            val json = intent.getStringExtra("data")
            taskCardData = Gson().fromJson(json, TaskCardData::class.java)
        }

        labelAdapter.dataset = dataset
        binding.createTaskStartDate.setOnClickListener(this)
        binding.createTaskEndDate.setOnClickListener(this)

        binding.createTaskStartTime.setOnClickListener(this)
        binding.createTaskEndTime.setOnClickListener(this)
        datePickerListener = this
        timePickerListener = this

        binding.fragmentCreateTasksRv.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        binding.fragmentCreateTasksRv.adapter = labelAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(applicationContext).inflate(R.menu.menu_create_task, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val dateTime = Calendar.getInstance().time.time.toString()
        val scope = CoroutineScope(Dispatchers.IO)
        when (item.itemId) {
            R.id.create_task_save -> {
                val note = binding.createTaskDetails.text.trim().toString()
                if (note == "") return false
                if (taskCardData != null) {
                    scope.launch {
                        val taskCardDatabase =
                            TaskCardDatabase.getDatabaseInstance(applicationContext)!!
                        val taskData = TaskData()
                        taskData.dateCreated = "$startTime $startDay"
                        taskData.deadline = "$endTime $endDay"
                        taskData.note = note
                        taskCardData.taskData.add(taskData)
                        taskCardDatabase.taskDao().updateTaskCard(taskCardData)
                        runOnUiThread { onBackPressed() }
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.create_task_start_date -> datePickerListener.startDate()
            R.id.create_task_end_date -> datePickerListener.endDate()
            R.id.create_task_start_time -> timePickerListener.startTime()
            R.id.create_task_end_time -> timePickerListener.endTime()
        }
    }

    override fun startDate() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        DatePickerDialog(this, startDatePickerDialog, year, month, day).show()
    }

    override fun endDate() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        DatePickerDialog(this, endDatePickerDialog, year, month, day).show()
    }

    override fun startTime() {
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        TimePickerDialog(this, startTimePickerDialog, hour, minute, false).show()
    }

    override fun endTime() {
        val minute = Calendar.getInstance().get(Calendar.MINUTE)
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        TimePickerDialog(this, endTimePickerDialog, hour, minute, false).show()
    }
}

interface DatePickerListener {
    fun startDate()
    fun endDate()
}

interface TimePickerListener {
    fun startTime()
    fun endTime()
}

class LabelAdapter : RecyclerView.Adapter<LabelAdapter.ViewHolder>() {
    lateinit var dataset: ArrayList<Int>
    private lateinit var context: Context

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view: ImageView = itemView.findViewById(R.id.create_task_row)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.create_task_label_row, null, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.view.setBackgroundColor(datum)
    }

    override fun getItemCount() = dataset.size
}