package ru.netology.nework.util

import kotlin.math.floor

fun numbersToString (num:Int):String = when (num){
    in 0..1099 -> num.toString()
    in 1100..9999 -> (floor(num.toDouble()/1000 * 10.0) / 10.0).toString() + "K"
    in 10000..999999 ->(num/1000).toString() + "K"
    else ->(floor(num.toDouble()/1000000 * 10.0) / 10.0).toString() + "M"
}


