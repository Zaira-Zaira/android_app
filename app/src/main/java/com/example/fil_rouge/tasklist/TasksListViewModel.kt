package com.example.fil_rouge.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fil_rouge.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksListViewModel : ViewModel {
    constructor();
    private val webService = Api.taskWebService
    // privée mais modifiable à l'intérieur du VM:
    private val _tasksStateFlow = MutableStateFlow<List<Task>>(emptyList())
    // même donnée mais publique et non-modifiable à l'extérieur afin de pouvoir seulement s'y abonner:
    public val tasksStateFlow: StateFlow<List<Task>> = _tasksStateFlow.asStateFlow()

    suspend fun refresh() {
        viewModelScope.launch {
            val tasksResponse = webService.getTasks() // Call HTTP (opération longue)
            if (tasksResponse.isSuccessful) { // à cette ligne, on a reçu la réponse de l'API
                val fetchedTasks = tasksResponse.body()!!
                _tasksStateFlow.value = fetchedTasks // on modifie le flow, ce qui déclenche ses observers
            }
        }
    }

    suspend fun create(task: Task){
        viewModelScope.launch {
            val response = webService.create(task)
            if (response.isSuccessful){
                val newTask = response.body()!!
                _tasksStateFlow.value = _tasksStateFlow.value + newTask
            }
        }
    }

     suspend fun update(task: Task){
         viewModelScope.launch {
             val response = webService.update(task, task.id);
             if (response.isSuccessful){
                 val updatedTask = response.body()!!
                 _tasksStateFlow.value = _tasksStateFlow.value - task + updatedTask
             }
         }
     }
     suspend fun delete(task: Task){
         viewModelScope.launch {
             val response = webService.delete(task.id);
             if (response.isSuccessful){
                 _tasksStateFlow.value = _tasksStateFlow.value - task
             }
         }
     }
}