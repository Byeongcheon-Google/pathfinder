package com.bcgg.pathfinder

import com.bcgg.model.Point
import java.time.LocalDate
import java.time.LocalTime

data class PathFinderResult(
    val foundTime: LocalTime,
    val date: LocalDate,
    val result: List<PathFinderItem>
) {
    sealed class PathFinderItem {
        data class Place(
            val name: String,
            val classification: Point.Classification,
            val position: LatLng,
            val stayTimeMinute: Long,
            val startTime: LocalTime
        ) : PathFinderItem()

        data class Move(
            val distance: Double,
            val distanceUnit: DistanceUnit,
            val points: List<LatLng>,
            val boundSouthWest: LatLng,
            val boundNorthEast: LatLng,
            val startTime: LocalTime,
            val durationMinute: Long
        ) : PathFinderItem()
    }

    enum class DistanceUnit {
        KM, M
    }

    data class LatLng(
        val lat: Double,
        val lng: Double
    )
}
