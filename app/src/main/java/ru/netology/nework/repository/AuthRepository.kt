package ru.netology.nework.repository

import ru.netology.nework.dto.User


interface AuthRepository {

    suspend fun authUser(login: String, pass: String): User

    suspend fun registrationUser(login: String, password: String, name: String): User

}