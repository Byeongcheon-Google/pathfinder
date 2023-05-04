package com.bcgg.pathfinder

import com.bcgg.di.Samples
import com.bcgg.di.ServiceLocator
import com.bcgg.model.PathFinderInput
import com.bcgg.model.Point
import com.bcgg.repository.DirectionsRepository
import kotlinx.coroutines.runBlocking
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.PriorityQueue
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Path finding class
 * Input: PathFinderInput
 * Output: List of Points
 *
 * Goals
 * 1. 사용자가 설정한 희망 파라미터(종료 희망 시간, 식사 희망 시간)에 최대한 근접
 * 2. 사용자가 설정한 시작과 끝 지점을 방문하며 주어진 위치 중 최대한 많은 위치를 방문
 *
 * 이게 잘 짜여진 경로인가? 고려사항
 * 1. 식사를 2번 하는 것으로 정했지만 이로 인해 여행지를 많이 방문하지 못할 경우
 * 2. 1200 식사를 원하는데 실제 1230 식사의 경우 이는 잘 짜여진 것인가?
 * 3.
 *
 * @see PathFinderInput
 * @see Point
 */
class PathFinder(
    val directionsRepository: DirectionsRepository,
    val input: PathFinderInput
) {

    var calculateEndTime: LocalTime? = null
        private set
    val isCalculated: Boolean get() = calculateEndTime != null
    private val allPoints = (input.points + listOf(input.startPoint, input.endPoint)).toSet()

    var minCost = Double.MAX_VALUE
        private set
    private val startTimeDouble = input.startTime.double
    private val endTimeDouble = input.endHopeTime.double
    private val mealTimesDouble = input.mealTimes.map { it.double }
    private var path: List<Point>? = null

    suspend fun getPath(): List<Pair<Point, ClosedRange<LocalTime>>> {
        if(path.isNullOrEmpty()) {
            runDfs(
                depth = 0,
                currentWeight = 1.0,
                visited = listOf(input.startPoint),
                mealTimes = listOf(),
                elapsedTimeHour = 0.0
            )

            calculateEndTime = LocalTime.now()
        }

        var startTime = startTimeDouble
        var previous: Point? = null

        return path!!.map { point ->
            if(previous != null) {
                startTime += directionsRepository.getMoveTime(allPoints, previous!!, point)
            }
            val startLocalTime = LocalTime.of(startTime.hour, startTime.minute, 0)
            startTime += point.stayTimeMinute / 60.0
            val endLocalTime = startLocalTime.plusMinutes(point.stayTimeMinute)

            previous = point

            point to startLocalTime..endLocalTime
        }
    }

    private suspend fun runDfs(
        depth: Int,
        currentWeight: Double,
        visited: Collection<Point>,
        mealTimes: List<Double>,
        elapsedTimeHour: Double
    ) {
        val endpointMoveTimeHour = directionsRepository.getMoveTime(allPoints, visited.last(), input.endPoint)

        val totalCost = getTotalCost(
            currentWeight = currentWeight + timeFunc(endpointMoveTimeHour),
            endTime = startTimeDouble + elapsedTimeHour + endpointMoveTimeHour,
            mealTimes = mealTimes
        )

        if(totalCost < minCost) {
            minCost = totalCost
            path = visited.plusElement(input.endPoint)
        }

        (input.points - visited.toSet()).forEach { willVisitPoint ->
            val moveTimeHour = directionsRepository.getMoveTime(allPoints, visited.last(), willVisitPoint)

            if(startTimeDouble + elapsedTimeHour + moveTimeHour >= endTimeDouble + END_TIME_THRESHOLD_HOUR) return@forEach

            val timeWeight = timeFunc(moveTimeHour)

            runDfs(
                depth = depth + 1,
                currentWeight = currentWeight + timeWeight,
                visited = visited.plusElement(willVisitPoint),
                mealTimes = if(willVisitPoint.isMeal) {
                    mealTimes.plusElement(startTimeDouble + elapsedTimeHour + moveTimeHour)
                } else {
                    mealTimes
                },
                elapsedTimeHour = elapsedTimeHour + moveTimeHour + willVisitPoint.stayTimeMinute / 60.0
            )
        }
    }

    private fun getTotalCost(
        currentWeight: Double,
        endTime: Double,
        mealTimes: List<Double>
    ): Double {
        val endTimeWeight = getEndTimeGaussian(endTimeDouble, endTime)

        return currentWeight * endTimeWeight * mealTimes.mealTimeWeight
    }

    private val List<Double>.mealTimeWeight: Double get() {
        var weight = 1.0
        mealTimesDouble.forEachIndexed { index, hour ->
            if(index > this.lastIndex) {
                weight *= MEAL_TIME_MAX_VALUE
                return@forEachIndexed
            }

            weight *= getMealTimeGaussian(hour, this[index])
        }

        repeat(size - mealTimesDouble.size) {
            weight *= MEAL_TIME_MAX_VALUE
        }

        return weight
    }

    private val Point.isStartPoint get() = this == input.startPoint
    private val Point.isEndPoint get() = this == input.endPoint

    private val LocalTime.double get() = this.hour + this.minute / 60.0
    private val Number.hour get() = this.toDouble() / 60
    private val Number.minute get() = this.toDouble() * 60

    private val Point.isMeal get() = this.classification == Point.Classification.Food
    private val Double.hour get() = toInt()
    private val Double.minute get() = ((this - toInt()) * 60).toInt()

    companion object {
        const val VERBOSE = true
        private const val MOVE_TIME_POW = 4
        private const val MOVE_TIME_MAX_VALUE = 8.0
        private const val MEAL_TIME_SIGMA = 0.5
        private const val MEAL_TIME_MAX_VALUE = 2
        private const val END_TIME_SIGMA = 0.5
        private const val END_TIME_MAX_VALUE = 16
        private const val END_TIME_THRESHOLD_HOUR = 2.0

        //Sigmoid 함수
        private fun sigmoid(x: Double): Double = 1 / (1 + exp(x))

        //시간 가중치 함수
        private fun timeFunc(x: Double): Double {
            val value = (x * 2).pow(MOVE_TIME_POW) + 0.35
            return if (value < MOVE_TIME_MAX_VALUE) value else MOVE_TIME_MAX_VALUE
        }

        //비용 가중치 함수
        //private fun costFunc(x: Double): Double = -sigmoid((x - 30000) / 30000) + 1

        /**
         * 어떤 시간이 식사 시간과 얼마나 멀리 떨어져 있는가를 가우시안 함수(like 정규분포)를 이용하여 반환하는 함수
         *
         * @param mean 원하는(의도한) 식사 시간(0.0 ~ 23.9999)
         * @param value 실제 시간(0.0 ~ 23.9999)
         *
         * @return 식사 시간 가우시안 값, 차이가 멀 수록 크게 낮은 값을 가짐
         */
        private fun getMealTimeGaussian(mean: Double, value: Double): Double {
            val maxValue = (1 / sqrt(2 * PI * MEAL_TIME_SIGMA * MEAL_TIME_SIGMA)) * exp(0.0)
            val result = (1 / sqrt(2 * PI * MEAL_TIME_SIGMA * MEAL_TIME_SIGMA)) *
                    exp(-(mean - value).pow(2) / 2 * MEAL_TIME_SIGMA * MEAL_TIME_SIGMA) /
                    maxValue

            return MEAL_TIME_MAX_VALUE - result * (MEAL_TIME_MAX_VALUE - 1)
        }

        /**
         * 어떤 시간이 여행 종료 시간과 얼마나 멀리 떨어져 있는가를 가우시안 함수(like 정규분포)를 이용하여 반환하는 함수
         *
         * @param mean 원하는(의도한) 식사 시간(0.0 ~ 23.9999)
         * @param value 실제 시간(0.0 ~ 23.9999)
         *
         * @return 끝나는 시간 가우시안 값, 차이가 멀 수록 크게 낮은 값을 가짐
         */
        private fun getEndTimeGaussian(mean: Double, value: Double): Double {
            val maxValue = (1 / sqrt(2 * PI * END_TIME_SIGMA * END_TIME_SIGMA)) * exp(0.0)
            val result = (1 / sqrt(2 * PI * END_TIME_SIGMA * END_TIME_SIGMA)) *
                    exp(-(mean - value).pow(2) / 2 * END_TIME_SIGMA * END_TIME_SIGMA) /
                    maxValue

            if(value in (mean - 0.75)..(mean + 0.25)) return 1.0

            return END_TIME_MAX_VALUE - result * (END_TIME_MAX_VALUE - 1)
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val arrayTime = Array(10) { it / 5.toDouble() }
            val arrayCost = Array(10) { if (it == 0) 0.0 else 10.0.pow((it + 4) / 2.5) }
            val arrayMealTime = Array(12) { it.toDouble() + 6 }
            val arrayEndTime = Array(12) { it.toDouble() / 2 + 9 }

            println("*Time Weight*")
            println(arrayTime.joinToString(separator = " ") { String.format("%8.1fh", it) })
            println(arrayTime.joinToString(separator = " ") { String.format("%9.3f", timeFunc(it)) })
            println()
            //println("*Cost Weight*")
            //println(arrayCost.joinToString(separator = " ") { String.format("%8.0f₩", it) })
            //println(arrayCost.joinToString(separator = " ") { String.format("%9.3f", costFunc(it)) })
            //println()
            println("*Meal Time Weight*")
            println(arrayMealTime.joinToString(separator = " ") { String.format("%4.0fh", it) })
            println(arrayMealTime.joinToString(separator = " ") {
                String.format(
                    "%3.3f",
                    getMealTimeGaussian(12.0, it)
                )
            })
            println()
            println("*End Time Weight*")
            println(arrayEndTime.joinToString(separator = " ") { String.format("%6.1fh", it) })
            println(arrayEndTime.joinToString(separator = " ") {
                String.format(
                    "%7.3f",
                    getEndTimeGaussian(12.0, it)
                )
            })

            println("===========Path finder test===========")
            val testPathFinderInput = PathFinderInput(
                startTime = LocalTime.of(10, 0, 0),
                endHopeTime = LocalTime.of(22, 0, 0),
                mealTimes = listOf(
                    LocalTime.of(12, 30, 0),
                    LocalTime.of(18, 0, 0),
                ),
                startPoint = Samples.sampleHouse1,
                endPoint = Samples.sampleHouse2,
                points = Samples.points,
            )

            val pathFinder = PathFinder(ServiceLocator.directionsRepository, testPathFinderInput)

            runBlocking {
                val format = DateTimeFormatter.ofPattern("HH:mm")
                val result = pathFinder.getPath()
                    .mapIndexed { index, (point, timeRange) ->  "#${index + 1} : $point\t\t${timeRange.start.format(format)} ~ ${timeRange.endInclusive.format(format)}" }
                    .joinToString(separator = "\n")

                println(result)
                println("Total Weight: ${pathFinder.minCost}")
            }
        }
    }
}