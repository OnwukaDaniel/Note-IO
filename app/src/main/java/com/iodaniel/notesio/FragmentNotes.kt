package com.iodaniel.notesio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.iodaniel.notesio.databinding.FragmentNotesBinding
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.utils.HomeViewModel

class FragmentNotes : Fragment(), View.OnClickListener {

    private val binding by lazy {
        FragmentNotesBinding.inflate(layoutInflater)
    }
    private lateinit var homeViewModel: HomeViewModel
    private var noteRecyclerView = NoteRecyclerView()
    private val orientation = RecyclerView.VERTICAL

    override fun onStart() {
        super.onStart()
        binding.homeCreateNote.setOnClickListener(this)


        homeViewModel = HomeViewModel()
        homeViewModel.getAllData(requireContext())!!.observe(this, { notes ->
            noteRecyclerView.dataset = notes as ArrayList<NoteData>
            noteRecyclerView.activity = requireActivity()
            binding.homeRv.layoutManager =
                LinearLayoutManager(requireContext(), orientation, false)
            binding.homeRv.adapter = noteRecyclerView
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.home_create_note -> {
                val intent = Intent(requireContext(), CreateNote::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }
        }
    }
}

class NoteRecyclerView : RecyclerView.Adapter<NoteRecyclerView.ViewHolder>() {
    lateinit var dataset: ArrayList<NoteData>
    private lateinit var context: Context
    lateinit var activity: Activity

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.note_row_title)
        val note: TextView = itemView.findViewById(R.id.note_row_note)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.note_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val datum = dataset[position]
        holder.title.text = datum.noteTitle
        holder.note.text = datum.note
        holder.itemView.setOnClickListener {
            val intent = Intent(context, CreateNote::class.java)
            val json = Gson().toJson(datum)
            intent.putExtra("note data", json)
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    override fun getItemCount() = dataset.size
}