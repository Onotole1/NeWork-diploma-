package ru.netology.nework.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.dto.User
import ru.netology.nework.model.UserModel
import ru.netology.nework.model.UsersModelState

import ru.netology.nework.repository.UserRepository
import javax.inject.Inject

class UserViewModel@Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    val data: LiveData<UserModel> = repository.data
        .map { user ->
            UserModel(
                user,
                user.isEmpty()
            )
        }.asLiveData(Dispatchers.Default)

   // private var profileId = stateHandle[USER_ID] ?: appAuth.authStateFlow.value.id

    val user = MutableLiveData<User>()
    private val _dataState = MutableLiveData<UsersModelState>()
    val dataState: LiveData<UsersModelState>
        get() = _dataState

    private val _usersIds = MutableLiveData<Set<Long>>()
    val userIds: LiveData<Set<Long>>
        get() = _usersIds

    init {
       // getUserById(profileId)
        loadUsers()
    }
    fun loadUsers() = viewModelScope.launch {
        try {
            _dataState.value = UsersModelState(loading = true)
            repository.getAll()
            _dataState.value = UsersModelState()
        } catch (e: Exception) {
            _dataState.value = UsersModelState(error = true)

        }
    }
    fun getUserById(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = UsersModelState(loading = true)
            repository.getById(id)
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
}