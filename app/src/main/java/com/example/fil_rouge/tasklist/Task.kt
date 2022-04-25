package com.example.fil_rouge.tasklist

import kotlinx.serialization.Serializable

@Serializable
data class Task(var id: String, var title : String, var description: String = "Lorem ipsum"): java.io.Serializable {

}