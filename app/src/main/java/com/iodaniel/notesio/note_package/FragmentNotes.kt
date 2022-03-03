package com.iodaniel.notesio.note_package

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.iodaniel.notesio.R
import com.iodaniel.notesio.databinding.FragmentNotesBinding
import com.iodaniel.notesio.room_package.NoteData
import com.iodaniel.notesio.view_model_package.HomeViewModel

class FragmentNotes : Fragment(), OnClickListener, NoteAvailabilityListener {

    private lateinit var binding: FragmentNotesBinding
    private lateinit var homeViewModel: HomeViewModel
    private var noteRvAdapter = NoteRecyclerView()
    private var dataset: ArrayList<NoteData> = arrayListOf()
    private lateinit var noteAvailabilityListener: NoteAvailabilityListener
    private val orientation = RecyclerView.VERTICAL

    override fun onStart() {
        super.onStart()
        binding.homeCreateNote.setOnClickListener(this)
        noteAvailabilityListener = this

        homeViewModel = HomeViewModel()
        homeViewModel.getAllData(requireContext())!!.observe(this, { notes ->
            dataset = notes as ArrayList<NoteData>
            noteRvAdapter.dataset = dataset
            noteRvAdapter.activity = requireActivity()
            binding.homeRv.layoutManager =
                LinearLayoutManager(requireContext(), orientation, false)
            if (dataset.isEmpty()) noteAvailabilityListener.noNote() else noteAvailabilityListener.notePresent()
            binding.homeRv.adapter = noteRvAdapter
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotesBinding.inflate(layoutInflater, container, false)
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

    override fun noNote() {
        binding.fragmentNoteRoot.visibility = View.VISIBLE
    }

    override fun notePresent() {
        binding.fragmentNoteRoot.visibility = View.GONE
    }
}

interface NoteAvailabilityListener {
    fun noNote()
    fun notePresent()
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