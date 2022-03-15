package com.iodaniel.notesio.task_card_package

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.FragmentTasksCardBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.room_package2.TaskCardDatabase
import com.iodaniel.notesio.task_card_package.TaskCardAdapter.ViewHolder
import com.iodaniel.notesio.utils.Util
import com.iodaniel.notesio.view_model_package.ViewModelTaskCards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FragmentTaskCards : Fragment(), TaskCardAvailabilityListener, OnClickListener {

    private lateinit var binding: FragmentTasksCardBinding
    private var taskCardAdapter = TaskCardAdapter()
    private lateinit var taskCardAvailabilityListener: TaskCardAvailabilityListener
    var dataset: ArrayList<TaskCardData> = arrayListOf()
    private var viewModelTaskCards = ViewModelTaskCards()
    private lateinit var taskCardDao: TaskCardDatabase
    val scope = CoroutineScope(Dispatchers.IO)

    override fun onStart() {
        super.onStart()
        taskCardAvailabilityListener = this
        binding.taskAvailabilityFab.setOnClickListener(this)
        taskCardDao = TaskCardDatabase.getDatabaseInstance(requireContext())!!

        viewModelTaskCards.getAllTaskCards(requireContext()).observe(this) {
            dataset = it as ArrayList<TaskCardData>
            taskCardAdapter.dataset = dataset
            taskCardAdapter.activity = requireActivity()
            binding.fragmentTasksRv.layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            binding.fragmentTasksRv.adapter = taskCardAdapter
            if (dataset.isEmpty()) taskCardAvailabilityListener.noTask() else taskCardAvailabilityListener.taskPresent()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTasksCardBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun noTask() {
        binding.taskAvailabilityRoot.visibility = View.VISIBLE
    }

    override fun taskPresent() {
        binding.taskAvailabilityRoot.visibility = View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.task_availability_fab -> {
                val dialogView = layoutInflater.inflate(R.layout.create_task_card, null)
                val taskCardData = TaskCardData()
                val alertBuilder = AlertDialog.Builder(requireContext())
                    .setTitle("Note Card Name")
                    .setView(dialogView)
                val dialog = alertBuilder.create()
                dialog.show()
                val title: EditText = dialogView.findViewById(R.id.task_card_title)
                val button: TextView = dialogView.findViewById(R.id.card_save)

                button.setOnClickListener {
                    scope.launch {
                        if (title.text.trim().toString() == "") return@launch
                        taskCardData.cardTitle = title.text.trim().toString()
                        requireActivity().runOnUiThread { dialog.dismiss() }
                        taskCardData.dateCreated = Calendar.getInstance().time.time.toString()
                        taskCardDao.taskDao().insertTaskCard(taskCardData)
                    }
                }
            }
        }
    }
}

interface TaskCardAvailabilityListener {
    fun noTask()
    fun taskPresent()
}

class TaskCardAdapter : RecyclerView.Adapter<ViewHolder>() {

    private lateinit var context: Context
    lateinit var activity: Activity
    lateinit var dataset: ArrayList<TaskCardData>
    private lateinit var taskCardDatabase: TaskCardDatabase
    val scope = CoroutineScope(Dispatchers.IO)
    val calenderInstance = Calendar.getInstance()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card_title: TextView = itemView.findViewById(R.id.task_card_row_title)
        val card_date_more: ImageView = itemView.findViewById(R.id.task_card_row_more)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        taskCardDatabase = TaskCardDatabase.getDatabaseInstance(context)!!
        val view = LayoutInflater.from(context).inflate(R.layout.task_card_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.card_title.text = datum.cardTitle
        calenderInstance.timeInMillis = datum.dateCreated.toLong()
        var minute = calenderInstance.get(Calendar.MINUTE).toString()
        minute = if (minute.length > 1) minute else "0$minute"
        val hour = calenderInstance.get(Calendar.HOUR)
        val day = calenderInstance.get(Calendar.DAY_OF_MONTH)
        val month = calenderInstance.get(Calendar.MONTH)
        val year = calenderInstance.get(Calendar.YEAR)
        val amPm = Util.digitToAmPm[calenderInstance.get(Calendar.AM_PM)]

        val time = "Created: $hour:$minute $amPm on $day/$month/$year"

        holder.card_date_more.setOnClickListener {
            val view = LayoutInflater.from(context).inflate(R.layout.note_card_more, null, true)
            val dim = WindowManager.LayoutParams.WRAP_CONTENT
            val popupWindow = PopupWindow(view, dim, dim, true)
            popupWindow.showAsDropDown(it, 0, -89, Gravity.CENTER)
            val details: ImageView = view.findViewById(R.id.note_card_more_details)
            val delete: ImageView = view.findViewById(R.id.note_card_more_delete_now)
            details.setOnClickListener { Snackbar.make(it, time, Snackbar.LENGTH_SHORT).show() }
            delete.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Are you sure you want to delete this card?")
                    .setPositiveButton("Delete") { dialog, which ->
                        scope.launch { taskCardDatabase.taskDao().deleteNote(datum) }
                        notifyItemRemoved(holder.adapterPosition)
                        popupWindow.dismiss()
                    }
                    .setNegativeButton("Cancel") { dialog, which ->
                        popupWindow.dismiss()
                    }.show()
            }
        }

        holder.itemView.setOnClickListener {
            val json = Gson().toJson(datum)
            val intent = Intent(context, ActivityTasks::class.java)
            intent.putExtra("data", json)
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount() = dataset.size
}