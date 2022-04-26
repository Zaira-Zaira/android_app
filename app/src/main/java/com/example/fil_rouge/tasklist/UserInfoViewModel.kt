package com.example.fil_rouge.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fil_rouge.network.Api
import com.example.fil_rouge.network.UserInfo
import kotlinx.coroutines.launch

class UserInfoViewModel : ViewModel {
    constructor();
    private val webService = Api.userWebService;

    suspend fun update(userInfo: UserInfo){
        viewModelScope.launch {
            val response = webService.update(userInfo);
            if (response.isSuccessful){
                val userinfo = response.body()!!

            }
        }
    }

    suspend fun refresh() {
        viewModelScope.launch {
            val response = webService.getInfo();
            if (response.isSuccessful) {
                val fetchedUserInfo = response.body()!!

            }
        }
    }
}