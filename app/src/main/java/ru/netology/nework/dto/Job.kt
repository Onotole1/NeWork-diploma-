package ru.netology.nework.dto

import ru.netology.nework.R

data class Job (
    val id: Long,
    val name: String,
    val position: String,
    val start: Long?,
    val finish: Long? = null,
    val link: String? = null,
    val ownerId: Long,
    val ownedByMe: Boolean=false,
        ){
    companion object {
        val emptyJob = Job(
            id = 0,
            name = "",
            position = "",
            start = 0L,
            finish = null,
            ownerId = 0L
        )
    }
    fun getString():String{
        var period =""
        if (finish!=null){
        period = start.toString()+" - " + finish.toString()
        } else {period = start.toString()+" " + R.string.periodWork }

        return (period)
    }
}