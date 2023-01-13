package ru.netology.nework.viewmodel

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.model.LoginFormState
import ru.netology.nework.model.FileModel
import ru.netology.nework.repository.auth.AuthRepository

import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val auth: AppAuth,
    private val repository: AuthRepository,
) : ViewModel() {

    private val _data = MutableLiveData<AuthState>()
    val data: LiveData<AuthState>
        get() = _data

    private val _loginForm = MutableLiveData<LoginFormState>()
    val loginFormState: LiveData<LoginFormState>
        get() = _loginForm

    private val noPhoto = FileModel()
    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<FileModel>
        get() = _photo
    private val _avatar = MutableLiveData(noPhoto)

    val avatar: LiveData<FileModel>
        get() = _avatar

    fun getToken(login: String, pass: String): PushToken {
        var token:PushToken= nullToken
        viewModelScope.launch {
                try {
                   val user = repository.updateUser(login, pass)
                    token= user.token!!
                } catch (e: Exception) {
                    _loginForm.postValue(LoginFormState(errorRegistration = true))
                }
            }
        return token
        }

    fun registrationUser(login: String, password: String, name: String) {
        viewModelScope.launch {
            try {
                when (_avatar.value) {
                    noPhoto -> {
                        val authState = repository.registrationUser(login, password, name)
                        auth.setAuth(authState.id, authState.token.toString(), login)
                    }
                    else -> {
                        _avatar.value?.file?.let { file ->
                            val authState = repository.registrationUserWithAvatar(login, password, name, MediaUploadFile(file))
                            auth.setAuth(authState.id, authState.token.toString(), login)
                        }
                    }
                }
            } catch (e: Exception) {
                _loginForm.postValue(LoginFormState(errorRegistration = true))
            }
        }
    }


    fun loginChanged(password: String) {
        if (!isPasswordValid(password)) {
            _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
        } else {
            _loginForm.value = LoginFormState(isDataValid = true)
        }
    }

    fun authUser(login: String, password: String) {
        viewModelScope.launch {
            try {
                val user = repository.updateUser(login, password)
                _data.value = user
            } catch (e: Exception) {
                _loginForm.postValue(LoginFormState(errorAuth = true))
            }
        }

    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 5
    }
    fun changeAvatar(uri: Uri?) = viewModelScope.launch {
        _avatar.value = FileModel(uri, uri?.toFile())
    }

}
