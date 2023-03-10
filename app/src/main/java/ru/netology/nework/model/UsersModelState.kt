package ru.netology.nework.model

import ru.netology.nework.dto.User


data class UserModel(
    val users: List<User> = emptyList(),
    val empty: Boolean = false,
    val retryId: Long = 0,
)

data class UsersModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
)

data class LoginFormState(
    val passwordError: Int? = null,
    val isDataValid: Boolean = false,
    val errorAuth: Boolean = false,
    val errorRegistration: Boolean = false,
    val loginError: Boolean = false,
)