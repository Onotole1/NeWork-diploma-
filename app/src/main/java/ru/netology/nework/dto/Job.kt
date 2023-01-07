package ru.netology.nework.dto

data class Job (
    val id: Long,
    val name: String,
    val position: String,
    val start: Long?,
    val finish: Long? = null,
    val link: String? = null,
    val ownedByMe: Boolean = false
        ){
    companion object {
        val emptyJob = Job(
            id = 0,
            name = "",
            position = "",
            start = 0L,
            finish = null
        )
    }
}