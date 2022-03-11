package com.iodaniel.notesio.task_card_package

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.FragmentTasksCardBinding
import com.iodaniel.notesio.room_package2.TaskCardData
import com.iodaniel.notesio.task_card_package.TaskCardAdapter.ViewHolder
import com.iodaniel.notesio.utils.Util
import com.iodaniel.notesio.view_model_package.ViewModelTaskCards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FragmentTaskCards : Fragment(), TaskCardAvailabilityListener, OnClickListener {

    private lateinit var binding: FragmentTasksCardBinding
    private var taskCardAdapter = TaskCardAdapter()
    private lateinit var taskCardAvailabilityListener: TaskCardAvailabilityListener
    var dataset: ArrayList<TaskCardData> = arrayListOf()
    private var viewModelTaskCards = ViewModelTaskCards()

    override fun onStart() {
        super.onStart()
        taskCardAvailabilityListener = this
        binding.taskAvailabilityFab.setOnClickListener(this)
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
                val intent = Intent(requireContext(), CreateTaskCard::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
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

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card_title: TextView = itemView.findViewById(R.id.task_card_row_title)
        val card_date_created: TextView = itemView.findViewById(R.id.task_card_row_date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.task_card_row, null, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.card_title.text = datum.cardTitle

        holder.card_date_created.text = Util.convertLongToDate(datum.dateCreated.toLong())

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
