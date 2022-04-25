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
        var validBtn = findViewById<Button>(R.id.validButton);
        var taskTitle = findViewById<EditText>(R.id.editTitle);
        var taskDescription = findViewById<EditText>(R.id.editDescription);
        validBtn.setOnClickListener{
            val newTask =
                Task(id = UUID.randomUUID().toString(), title = taskTitle.getText().toString(), description = taskDescription.getText().toString());
            intent.putExtra("task", newTask);
            setResult(RESULT_OK, intent);
            finish();
        }

    }
}