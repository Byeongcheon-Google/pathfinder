package com.bcgg.util

operator fun CharSequence.times(times: Int) : CharSequence{
    val stringBuilder = StringBuilder()
    repeat(times) {
        stringBuilder.append(this)
    }
    return stringBuilder.toString()
}