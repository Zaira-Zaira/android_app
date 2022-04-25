package com.example.fil_rouge.tasklist


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.LogPrinter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fil_rouge.R
import com.example.fil_rouge.databinding.FragmentTaskListBinding
import com.example.fil_rouge.form.FormActivity
import java.util.logging.Logger


class TaskListFragment : Fragment() {
    private var taskList = listOf<Task>(
        Task(id = "id_1", title ="Task 1", description = "Description 1"),
        Task(id = "id_2", title ="Task 2"),
        Task(id = "id_3", title ="Task 3"),
    )
    val myAdapter = TaskListAdapter();

    private var _binding : FragmentTaskListBinding? = null
    val formLaucher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {result ->
        var task = result.data?.getSerializableExtra("task") as? Task ?: return@registerForActivityResult
        taskList = taskList + task;
        myAdapter.submitList(taskList);

    }
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        var view = binding.root
        return view;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.task_title)
        recyclerView.layoutManager = LinearLayoutManager(context);
        var addBtn = view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.floatingActionButton);
        var deleteTask = view.findViewById<ImageButton>(R.id.delete_task);
        addBtn.setOnClickListener {
            var intent = Intent(context, FormActivity::class.java);
            formLaucher.launch(intent);
        }
        myAdapter.onClickDelete = {task ->
            taskList = taskList - task
            myAdapter.submitList(taskList);
        };
        recyclerView.adapter = myAdapter;
        myAdapter.submitList(taskList);

    }

}