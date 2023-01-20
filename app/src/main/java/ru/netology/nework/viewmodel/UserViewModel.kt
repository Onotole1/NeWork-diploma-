package ru.netology.nework.viewmodel

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.dto.User
import ru.netology.nework.enumeration.RetryTypes
import ru.netology.nework.model.UserModel
import ru.netology.nework.model.UsersModelState
import ru.netology.nework.repository.UserRepository
import java.io.IOException

import javax.inject.Inject

@HiltViewModel
class UserViewModel@Inject constructor(
    private val repository: UserRepository,
) : ViewModel() {

    var lastId: Long? = null
    var lastAction: RetryTypes? = null

    val user = MutableLiveData<User>()

    val data: LiveData<UserModel> = repository.data
        .map { user ->
            UserModel(
                user,
                user.isEmpty()
            )
        }.asLiveData(Dispatchers.Default)

    fun tryAgain() {
        when (lastAction) {
            RetryTypes.LOAD -> retryGetAllUsers()
            RetryTypes.GETBYID -> retryGetById()
            else -> retryGetAllUsers()
        }
    }

    private val _dataState = MutableLiveData<UsersModelState>()
    val dataState: LiveData<UsersModelState>
        get() = _dataState

    private val _usersIds = MutableLiveData<Set<Long>>()
    val userIds: LiveData<Set<Long>>
        get() = _usersIds

    init {
        loadUsers()
    }
    fun loadUsers() = viewModelScope.launch {
        lastAction = RetryTypes.LOAD
        try {
            _dataState.value = UsersModelState(loading = true)
            repository.getAllUsers()
            _dataState.value = UsersModelState()
        } catch (e: Exception) {
            _dataState.value = UsersModelState(error = true)
        }
    }

    fun retryGetAllUsers() {
        loadUsers()
    }

    private fun getUserById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = UsersModelState(loading = true)
            user.value = repository.getUserById(id)
            _dataState.value = UsersModelState()
        } catch (e: Exception) {
            _dataState.value = UsersModelState(error = true)
        }
    }

    fun getUserName(id: Long): String? {
        data.value?.users?.map { user ->
            if (id == user.id) return user.name
        }
        return null
    }

    fun getUserAvatar(id: Long): String? {
        data.value?.users?.map { user ->
            if (id == user.id) return user.avatar
        }
        return null
    }

    fun getUsersIds(set: Set<Long>) = viewModelScope.launch {
        _usersIds.value = set
    }

    fun retryGetById() {
        lastId?.let {
            getUserById(it)
        }
    }

}