package com.bcgg

import java.time.LocalTime
import java.util.Locale

data class Spot(
        val latitude: Double,
        val longitude: Double,
        val type: Type = Type.House,
        val stayTimeHour: Int = 1
) {
    enum class Type {
        House, Tour, Food
    }
}
