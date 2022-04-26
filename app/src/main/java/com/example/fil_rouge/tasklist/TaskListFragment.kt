package com.example.fil_rouge.tasklist

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import coil.transform.CircleCropTransformation
import com.example.fil_rouge.R
import com.example.fil_rouge.databinding.FragmentTaskListBinding
import com.example.fil_rouge.form.FormActivity
import com.example.fil_rouge.network.Api
import com.example.fil_rouge.user.UserInfoActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch


class TaskListFragment : Fragment() {

    private var taskList = listOf<Task>(
        Task(id = "id_1", title = "Task 1", description = "Description 1"),
        Task(id = "id_2", title = "Task 2"),
        Task(id = "id_3", title = "Task 3"),
    )
    private val myAdapter = TaskListAdapter();
    private val viewModel: TasksListViewModel by viewModels()
    private val userViewModel: UserInfoViewModel by viewModels()
    private var _binding: FragmentTaskListBinding? = null
    val createTask =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            var task = result.data?.getSerializableExtra("task") as? Task
                ?: return@registerForActivityResult
            lifecycleScope.launch { // on lance une coroutine car `collect` est `suspend`
                viewModel.create(task);
            }
            myAdapter.submitList(taskList);
        }

    val editTask =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            var task = result.data?.getSerializableExtra("task") as? Task
                ?: return@registerForActivityResult
            lifecycleScope.launch { // on lance une coroutine car `collect` est `suspend`
                viewModel.update(task);
            }
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

    override fun onResume() {
        super.onResume();
        val imageView = view?.findViewById<ImageView>(R.id.avatar);
        val intent = Intent(context, UserInfoActivity::class.java);
        val addAvatar = view?.findViewById<Button>(R.id.get_avatar);
        lifecycleScope.launch {
            viewModel.refresh();
        }

        lifecycleScope.launch {
            userViewModel.refresh();
        }

        imageView?.setOnClickListener{
            startActivity(intent);
        }
        imageView?.load("https://goo.gl/gEgYUd"){
            transformations(CircleCropTransformation());
        }

        lifecycleScope.launch {
            val response = Api.userWebService.getInfo();
            if(response.isSuccessful) {
                val res = response.body()!!
                imageView?.load(res.avatar) {
                    error(R.drawable.ic_launcher_background)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.task_title)
        val addAvatar = view.findViewById<Button>(R.id.get_avatar);

        recyclerView.layoutManager = LinearLayoutManager(context);
        var addBtn =
            view.findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(
                R.id.floatingActionButton
            );
        var deleteTask = view.findViewById<ImageButton>(R.id.delete_task);
        addBtn.setOnClickListener {
            var intent = Intent(context, FormActivity::class.java);
            createTask.launch(intent);
        }
        myAdapter.onClickDelete = { task ->
            lifecycleScope.launch { // on lance une coroutine car `collect` est `suspend`
                viewModel.delete(task);
            }
            myAdapter.submitList(taskList);
        };

        myAdapter.onClickEdit = { task ->
            var intent = Intent(context, FormActivity::class.java);
            intent.putExtra("task", task);
            editTask.launch(intent);
        };

        recyclerView.adapter = myAdapter;
        myAdapter.submitList(taskList);

        val userFirstname = view.findViewById<TextView>(R.id.userFirstname);
        val userLastname = view.findViewById<TextView>(R.id.userLastname);
        val userMail = view.findViewById<TextView>(R.id.userMail);
        val editUserInfo = view.findViewById<Button>(R.id.edit_userInfo);
        val intent = Intent(context, UserInfoActivity::class.java);
        editUserInfo.setOnClickListener{
            startActivity(intent);
        }

        lifecycleScope.launch {
            val response = Api.userWebService.getInfo()
            if (response.isSuccessful) {
                val userInfo = response.body()!!
                userFirstname.text = "${userInfo.firstName}";
                userLastname.text = "${userInfo.lastName}";
                userMail.text = "${userInfo.email}";
            } else {
                Log.e("Blqblq", response.message())
            }
        }


        lifecycleScope.launch { // on lance une coroutine car `collect` est `suspend`
            viewModel.tasksStateFlow.collect { newList ->
                taskList = newList;
                myAdapter.submitList(taskList);
            }
        }
    }

}