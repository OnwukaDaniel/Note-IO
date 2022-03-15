package com.iodaniel.notesio.task_card_package

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.IBinder
import android.view.*
import android.view.View.OnClickListener
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.ActivityCreateTaskBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.room_package2.TaskData
import com.iodaniel.notesio.utils.Util
import kotlinx.coroutines.*
import java.util.*

class ActivityCreateTask : AppCompatActivity(), OnClickListener, DatePickerListener,
    TimePickerListener, ColorPickerListener, ActivityCreateTaskViewTypeListener {

    private lateinit var binding: ActivityCreateTaskBinding
    private lateinit var datePickerListener: DatePickerListener
    private lateinit var timePickerListener: TimePickerListener
    private lateinit var activityCreateTaskViewTypeListener: ActivityCreateTaskViewTypeListener
    private lateinit var taskCardData: TaskCardData
    private lateinit var settingsPref: SharedPreferences
    private lateinit var taskData: TaskData
    private var taskDataPosition = 0
    private var labelColor = 0

    private var startSecs = ""
    private var startMin = ""
    private var startHour = ""

    private var startDay = ""
    private var startMonth = ""
    private var startYear = ""

    private var endSecs = ""
    private var endMin = ""
    private var endHour = ""

    private var endDay = ""
    private var endMonth = ""
    private var endYear = ""

    private var startTime = ""
    private var endTime = ""
    private var startDate = ""
    private var endDate = ""

    private val startTimePickerDialog = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        startSecs = Calendar.getInstance().get(Calendar.SECOND).toString()
        startMin = minute.toString()
        startHour = hourOfDay.toString()
        val pair = Util.convert24HrTo12Hr(hourOfDay)

        startTime = "${pair.second}:$minute ${pair.first}"
        binding.createTaskStartTime.text = startTime
    }

    private val endTimePickerDialog = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
        endSecs = Calendar.getInstance().get(Calendar.SECOND).toString()
        endMin = minute.toString()
        endHour = hourOfDay.toString()
        val pair = Util.convert24HrTo12Hr(hourOfDay)

        endTime = "${pair.second}:$minute ${pair.first}"
        binding.createTaskEndTime.text = endTime
    }

    private val startDatePickerDialog =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            startDay = dayOfMonth.toString()
            startMonth = month.toString()
            startYear = year.toString()

            startDate = "$dayOfMonth, ${Util.months[month]}, $year"
            binding.createTaskStartDate.text = startDate
        }

    private val endDatePickerDialog =
        DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            endDay = dayOfMonth.toString()
            endMonth = month.toString()
            endYear = year.toString()

            endDate = "$dayOfMonth, ${Util.months[month]}, $year"
            binding.createTaskEndDate.text = endDate
        }

    private var labelAdapter = LabelAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createTaskStartDate.setOnClickListener(this)
        binding.createTaskEndDate.setOnClickListener(this)

        binding.createTaskStartTime.setOnClickListener(this)
        binding.createTaskEndTime.setOnClickListener(this)
        datePickerListener = this
        timePickerListener = this
        activityCreateTaskViewTypeListener = this
        settingsPref = getSharedPreferences(
            getString(R.string.SETTINGS_SHAREDPREFERENCE),
            Context.MODE_PRIVATE
        )

        if (intent.hasExtra("data")) {
            val json = intent.getStringExtra("data")
            taskCardData = Gson().fromJson(json, TaskCardData::class.java)
            title = taskCardData.cardTitle
        }
        when (intent.hasExtra("taskData")) {
            true -> {
                val json = intent.getStringExtra("taskData")
                taskDataPosition = intent.getIntExtra("taskDataPosition", 0)
                taskData = Gson().fromJson(json, TaskData::class.java)
                labelColor = taskData.color
                title = taskCardData.cardTitle
                binding.createTaskDetails.setText(taskData.note)
                startTime = taskData.startDate
                endTime = taskData.deadline
                startDate = taskData.startDate
                endDate = taskData.deadline

                val startInstance = Calendar.getInstance()
                startInstance.timeInMillis = taskData.startDate.toLong()
                val endInstance = Calendar.getInstance()
                endInstance.timeInMillis = taskData.deadline.toLong()

                val startAmPm = startInstance.get(Calendar.AM_PM)
                var startMinute = startInstance.get(Calendar.MINUTE).toString()
                val startHour = startInstance.get(Calendar.HOUR)
                val startDay = startInstance.get(Calendar.DAY_OF_MONTH)
                val startMonth = startInstance.get(Calendar.MONTH)
                val startYear = startInstance.get(Calendar.YEAR)

                val endAmPm = startInstance.get(Calendar.AM_PM)
                var endMinute = endInstance.get(Calendar.MINUTE).toString()
                val endHour = endInstance.get(Calendar.HOUR)
                val endDay = endInstance.get(Calendar.DAY_OF_MONTH)
                val endMonth = endInstance.get(Calendar.MONTH)
                val endYear = endInstance.get(Calendar.YEAR)

                startMinute = if (startMinute.length == 1) "0$startMinute" else startMinute
                endMinute = if (endMinute.length == 1) "0$endMinute" else endMinute

                val sTime = "$startHour:$startMinute ${Util.digitToAmPm[startAmPm]}"
                val eTime = "$endHour:$endMinute ${Util.digitToAmPm[endAmPm]}"

                val startDateMod1 = "$startDay, ${Util.months[startMonth]}, $startYear"
                val endDateMod1 = "$endDay, ${Util.months[endMonth]}, $endYear"

                binding.createTaskStartTime.text = sTime
                binding.createTaskEndTime.text = eTime
                binding.createTaskEndDate.text = endDateMod1
                binding.createTaskStartDate.text = startDateMod1
            }
            false -> {
                taskData = TaskData()
                taskData.color = Color.Transparent.toArgb()
            }
        }

        labelAdapter.dataset = Util.taskLabelData
        labelAdapter.color = labelColor
        labelAdapter.colorPickerListener = this
        binding.fragmentCreateTasksRv.layoutManager =
            LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        binding.fragmentCreateTasksRv.adapter = labelAdapter
    }

    private fun saveTask() = runBlocking {
        val dateTime = Calendar.getInstance().time.time.toString()
        val scope = CoroutineScope(Dispatchers.IO)
        val note = binding.createTaskDetails.text!!.trim().toString()
        val taskData = TaskData()
        val startTime = getStartDateTime()
        val endTime = getEndDateTime()
        if (note == "") return@runBlocking
        if (startTime == "") return@runBlocking
        if (endTime == "") return@runBlocking
        val job = scope.async {
            val taskCardDatabase = TaskCardDatabase.getDatabaseInstance(applicationContext)!!
            taskData.dateCreated = dateTime
            taskData.startDate = startTime
            taskData.deadline = endTime
            taskData.color = labelColor
            taskData.note = note
            taskData.expired = false
            taskCardData.taskData.add(taskData)
            taskCardDatabase.taskDao().updateTaskCard(taskCardData)

            val notify =
                settingsPref.getBoolean(getString(R.string.NOTIFICATION_SHAREDPREFERENCE), false)
            if (notify) {
                val startEndTimeData =
                    StartEndTimeData(startTime.toLong(), endTime.toLong(), taskCardData)
                val taskJson = Gson().toJson(startEndTimeData)
                val serviceIntent = Intent(applicationContext, NotificationService::class.java)
                serviceIntent.putExtra(getString(R.string.TIME_PAIR_STRING), taskJson)
                startService(serviceIntent)
            }
        }
        job.join()
        runOnUiThread {
            val json = Gson().toJson(taskCardData)
            val intent = Intent(applicationContext, ActivityTasks::class.java)
            intent.putExtra("data", json)
            startActivity(intent)
            overridePendingTransition(0, 0)
        }
    }

    private fun getStartDateTime(): String {
        if (startYear == "" || startMonth == "" || startDay == "" || startHour == "" || startMin == "") return ""
        val calender = Calendar.getInstance()
        calender.set(
            startYear.toInt(),
            startMonth.toInt(),
            startDay.toInt(),
            startHour.toInt(),
            startMin.toInt()
        )
        return calender.timeInMillis.toString()
    }

    private fun getEndDateTime(): String {
        if (endYear == "" || endMonth == "" || endDay == "" || endHour == "" || endMin == "") return ""
        val calender = Calendar.getInstance()
        calender.set(
            endYear.toInt(),
            endMonth.toInt(),
            endDay.toInt(),
            endHour.toInt(),
            endMin.toInt()
        )
        return calender.timeInMillis.toString()
    }

    /*private fun updateNote() {
        val scope = CoroutineScope(Dispatchers.IO)
        val note = binding.createTaskDetails.text!!.trim().toString()
        if (note == "") return
        scope.launch {
            val taskCardDatabase = TaskCardDatabase.getDatabaseInstance(applicationContext)!!
            val taskData = TaskData()
            taskData.startDate = getStartDateTime()
            taskData.deadline = getEndDateTime()
            taskData.note = note
            taskData.color = labelColor
            taskCardData.taskData[taskDataPosition] = taskData
            taskCardDatabase.taskDao().updateTaskCard(taskCardData)
            runOnUiThread {
                val intent = Intent(applicationContext, ActivityTasks::class.java)
                val json = Gson().toJson(taskCardData)
                intent.putExtra("data", json)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }*/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(applicationContext).inflate(R.menu.menu_create_task, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_task_save -> if (intent.hasExtra("taskData")) return true else saveTask()
            R.id.create_task_delete -> if (intent.hasExtra("taskData")) deleteTask() else return false
        }
        return super.onOptionsItemSelected(item)
    }

    private fun keepEditingDialog() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Stop editing?")
            .setItems(arrayOf("Keep editing", "Discard")) { dialog, which ->
                when (which) {
                    0 -> dialog.dismiss()
                    1 -> super.onBackPressed()
                }
            }.show()
    }

    private fun deleteTask() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val taskCardDatabase = TaskCardDatabase.getDatabaseInstance(applicationContext)!!
            taskCardData.taskData.remove(taskData)
            taskCardDatabase.taskDao().updateTaskCard(taskCardData)
            runOnUiThread { super.onBackPressed() }
        }
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
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        DatePickerDialog(this, startDatePickerDialog, year, month, day).show()
    }

    override fun endDate() {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
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

    override fun setColorPicked(color: Int) {
        labelColor = color
    }

    override fun onBackPressed() {
        val details = binding.createTaskDetails.text.toString().trim()
        val sTime = binding.createTaskStartTime.text.toString().trim()
        val eTime = binding.createTaskEndTime.text.toString().trim()
        val sDate = binding.createTaskStartDate.text.toString().trim()
        val eDate = binding.createTaskEndDate.text.toString().trim()
        if (details != "" || sTime != "" || eTime != "" || sDate != "" || eDate != "") keepEditingDialog() else super.onBackPressed()
    }

    override fun inputDisabled() {
        binding.createTaskEndDate.isEnabled = false
        binding.createTaskStartDate.isEnabled = false
        binding.createTaskStartTime.isEnabled = false
        binding.createTaskEndTime.isEnabled = false
        binding.createTaskDetails.isEnabled = false
        binding.fragmentCreateTasksRv.isEnabled = false
    }
}

interface ActivityCreateTaskViewTypeListener {
    fun inputDisabled()
}

interface DatePickerListener {
    fun startDate()
    fun endDate()
}

interface TimePickerListener {
    fun startTime()
    fun endTime()
}

class StartEndTimeData(
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val taskCardData: TaskCardData,
)

class NotificationService : Service() {
    private val channelId = "Notification channelID"
    private lateinit var taskDataJson: String

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val scope = CoroutineScope(Dispatchers.IO)
        val json = intent!!.getStringExtra(applicationContext.getString(R.string.TIME_PAIR_STRING))
        val startEndTimeData: StartEndTimeData = Gson().fromJson(json, StartEndTimeData::class.java)

        runBlocking {
            scope.launch {
                taskDataJson = Gson().toJson(startEndTimeData.taskCardData)
                val timeElapsed = startEndTimeData.endTime - startEndTimeData.startTime
                val seconds = timeElapsed / 1000
                val minutes = seconds / 60
                val hours = minutes / 60
                val days = hours / 24
                var timeLeft = seconds
                val cardTitle =
                    startEndTimeData.taskCardData.taskData[startEndTimeData.taskCardData.taskData.size - 1].note
                val builder = createNotification("Note IO Task", "Complete your task")
                startForeground(1, builder.build())

                while (timeLeft > 0) {
                    delay(1000)
                    println("THIS IS THE CURRENT TIME ***************$timeLeft")
                    timeLeft -= 1L
                }
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun createNotification(title: String, text: String): NotificationCompat.Builder {
        val intentP = Intent(applicationContext, ActivityTasks::class.java)
        intentP.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intentP.putExtra("data", taskDataJson)
        val pendingIntent = PendingIntent.getActivities(
            applicationContext, 0,
            arrayOf(intentP), PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder = createNotification("Note IO Task", "Task expired")
            .setAutoCancel(true)
        with(manager) { notify(2, builder.build()) }
    }
}

class LabelAdapter : RecyclerView.Adapter<LabelAdapter.ViewHolder>() {
    lateinit var dataset: ArrayList<Int>
    lateinit var colorPickerListener: ColorPickerListener
    var color: Int = 0
    private lateinit var context: Context


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var view: View = itemView.findViewById(R.id.create_task_row)
        var tick: ImageView = itemView.findViewById(R.id.create_task_row_tick)
        var labels: TextView = itemView.findViewById(R.id.create_task_label_row_type)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view =
            LayoutInflater.from(context).inflate(R.layout.create_task_label_row, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SwitchIntDef", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.view.background = ColorDrawable(datum)
        holder.labels.text = Util.todoLabels[position]

        println("THIS IS THE COLOR OF THE LABEL *********  $datum ******************** $color")

        // TICK LOGIC
        if (datum != color) holder.tick.visibility = View.GONE else {
            holder.tick.visibility = View.VISIBLE
        }
        holder.itemView.setOnClickListener {
            if (holder.tick.visibility == View.VISIBLE) {
                holder.tick.visibility = View.GONE
                colorPickerListener.setColorPicked(0)
            } else if (holder.tick.visibility == View.GONE) {
                holder.tick.visibility = View.VISIBLE
                color = dataset[holder.adapterPosition]
                colorPickerListener.setColorPicked(color)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = dataset.size
}

interface ColorPickerListener {
    fun setColorPicked(color: Int)
}