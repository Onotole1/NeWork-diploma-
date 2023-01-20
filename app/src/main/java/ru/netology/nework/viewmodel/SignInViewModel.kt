package ru.netology.nework.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.model.AuthState
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.auth.AuthRepository

import javax.inject.Inject

class SignInViewModel   @Inject constructor(
    val auth: AppAuth,
    private val repository: AuthRepository
) : ViewModel() {

    private val _data = MutableLiveData<AuthState>()
    val data: LiveData<AuthState>
        get() = _data

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state

    fun loginAttempt(login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.updateUser(login, password)
                _data.value = user
            } catch (e: Exception) {
                _state.postValue(FeedModelState(loginError = true))
            }
        }
    }

}