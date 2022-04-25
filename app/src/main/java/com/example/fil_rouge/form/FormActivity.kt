package com.example.fil_rouge.form

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.fil_rouge.R
import com.example.fil_rouge.tasklist.Task
import java.util.*

class FormActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val validBtn = findViewById<Button>(R.id.validButton);
        val taskTitle = findViewById<EditText>(R.id.editTitle);
        val taskDescription = findViewById<EditText>(R.id.editDescription);
        val task = intent.getSerializableExtra("task") as? Task
        val id = task?.id ?: UUID.randomUUID().toString();

        taskTitle.setText(task?.title)
        taskDescription.setText(task?.description)

        validBtn.setOnClickListener{
                val newTask =
                    Task(id = id, title = taskTitle.text.toString(), description = taskDescription.text.toString());
                intent.putExtra("task", newTask);
                setResult(RESULT_OK, intent);
                finish();
            }
    }
}