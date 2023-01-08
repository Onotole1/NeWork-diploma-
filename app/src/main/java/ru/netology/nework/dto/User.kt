package ru.netology.nework.dto

data class User(
    val id: Long,
    val login: String,
    val name: String,
    val avatar: String? = null
) {
    companion object {
        val nuller = User(
            id = 0,
            login= "nuller",
            name = "nuller"


        )
    }
}