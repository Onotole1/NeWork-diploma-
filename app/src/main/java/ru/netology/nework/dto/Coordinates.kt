package ru.netology.nework.dto

import com.google.gson.annotations.SerializedName

data class Coordinates(
    @SerializedName("lat")
    val latitude: Double = 0.0,
    @SerializedName("long")
    val longitude: Double = 0.0,
)