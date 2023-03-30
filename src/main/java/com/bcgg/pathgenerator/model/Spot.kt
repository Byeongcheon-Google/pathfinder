package com.bcgg.pathgenerator.model

sealed class Spot(
    open val latitude: Double,
    open val longitude: Double,
) {
    data class Tour @JvmOverloads constructor(
        override val latitude: Double,
        override val longitude: Double,
        val stayTimeHour: Int = 1
    ) : Spot(latitude, longitude)

    data class Food(
        override val latitude: Double,
        override val longitude: Double
    ) : Spot(latitude, longitude)

    data class House(
        override val latitude: Double,
        override val longitude: Double
    ) : Spot(latitude, longitude), SpotGroup

    data class Normal(
        override val latitude: Double,
        override val longitude: Double
    ) : Spot(latitude, longitude)

    data class K(
        override val latitude: Double,
        override val longitude: Double
    ) : Spot(latitude, longitude), SpotGroup {
        operator fun minus(other: Spot) = K(this.latitude - other.latitude, this.longitude - other.longitude)
        operator fun plus(other: Spot) = K(this.latitude + other.latitude, this.longitude + other.longitude)
    }
}

interface SpotGroup