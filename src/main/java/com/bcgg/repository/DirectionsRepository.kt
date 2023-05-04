package com.bcgg.repository

import com.bcgg.api.NaverDirectionsApi
import com.bcgg.di.Samples.point1
import com.bcgg.di.Samples.point2
import com.bcgg.di.Samples.points
import com.bcgg.di.ServiceLocator
import com.bcgg.model.Point
import com.bcgg.source.DirectionsLocalDataSource
import kotlinx.coroutines.runBlocking
import kotlin.math.ceil

class DirectionsRepository(
    private val directionsLocalDataSource: DirectionsLocalDataSource,
    private val naverDirectionsApi: NaverDirectionsApi
) {
    suspend fun getMoveTime(allPoints: Set<Point>, point1: Point, point2: Point): Double {
        if (point1 !in allPoints) throw IllegalArgumentException("point 1 is not contains in given all paths")
        if (point2 !in allPoints) throw IllegalArgumentException("point 2 is not contains in given all paths")

        if (point1 == point2) return 0.0

        val duration = directionsLocalDataSource.getDuration(point1, point2)

        if (duration != null) return duration * TIME_PERCENTAGE

        val searchPath = generateNaverDirectionsApiWaypoints(allPoints, point1, point2)

        val start = searchPath.first().toNaverMapApiRequest()
        val waypoints =
            searchPath.slice(1 until searchPath.size).joinToString(separator = "|") { it.toNaverMapApiRequest() }
        val end = searchPath.last().toNaverMapApiRequest()

        val directionsResponse = naverDirectionsApi.directions5(
            start = start,
            goal = end,
            waypoints = waypoints
        )
        if (directionsResponse.code != 0) throw RuntimeException(directionsResponse.message)

        val durations = mutableListOf<Double>()

        with(directionsResponse.route!!.traoptimal[0].summary) {
            if (this.waypoints != null) {
                durations.addAll(this.waypoints.map { it.duration / 1000.0 / 60 / 60 })
            }

            val duration = goal.duration / 1000.0 / 60 / 60

            durations.add(ceil(duration * TIME_PERCENTAGE * 6) / 6.0)
        }

        directionsLocalDataSource.put(searchPath, durations)

        return (durations[0])
    }

    private fun generateNaverDirectionsApiWaypoints(
        allPoints: Set<Point>,
        point1: Point,
        point2: Point
    ): List<Point> {
        val temp = mutableListOf(point1, point2)
        var linearDistance = point1 distanceTo point2

        while (linearDistance < 1500 && temp.size < 7) {
            var added = false
            for (point in allPoints - temp.toSet()) {
                if (!directionsLocalDataSource.containsPoint(temp.last(), point)) {
                    linearDistance += temp.last() distanceTo point
                    if (linearDistance < 1500 && temp.size < 17) {
                        added = true
                        temp.add(point)
                    }
                    break
                }
            }

            if (!added) break
        }

        return temp
    }

    private fun Point.toNaverMapApiRequest() = "$lon,$lat"

    companion object {
        private const val TIME_PERCENTAGE = 1.1
        @JvmStatic
        fun main(args: Array<String>) {
            runBlocking {
                println(ServiceLocator.directionsRepository.getMoveTime(points, point1, point2))
                println(ServiceLocator.directionsRepository.getMoveTime(points, point1, point2))
            }
        }
    }
}