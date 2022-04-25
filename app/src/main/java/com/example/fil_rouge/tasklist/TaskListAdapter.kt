package com.example.fil_rouge.tasklist

import android.media.Image
import android.renderscript.ScriptGroup
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fil_rouge.R

object ListDiffCallBack: DiffUtil.ItemCallback<Task>(){
    override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
        return oldItem == newItem
    }
}

class TaskListAdapter : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(ListDiffCallBack) {
    var onClickDelete: (Task) -> Unit = {};
    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deleteBtn = itemView.findViewById<ImageButton>(R.id.delete_task);
        fun bind(task: Task) {
            var taskView = itemView.findViewById<TextView>(R.id.task_title);
            taskView.text = task.title;
            var taskDesc = itemView.findViewById<TextView>(R.id.task_description);
            taskDesc.text = task.description
        }
        fun deleteTask(task: Task) {
            deleteBtn.setOnClickListener{
                onClickDelete(task);
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView);
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
         holder.bind(getItem(position));
         holder.deleteTask(getItem(position));
    }
}
