package com.bcgg.util

import com.bcgg.pathgenerator.model.Spot
import kotlin.math.cos
import kotlin.math.sin

object VectorUtil {
    private fun rotateVector(vector: DoubleArray, angle: Double): DoubleArray {
        val rotatedVector = DoubleArray(2)
        val cosAngle = cos(angle)
        val sinAngle = sin(angle)
        rotatedVector[0] = vector[0] * cosAngle - vector[1] * sinAngle
        rotatedVector[1] = vector[0] * sinAngle + vector[1] * cosAngle
        return rotatedVector
    }

    @JvmStatic
    fun rotatedKSpot(kSpot: Spot.K, midSpot: Spot, angle: Double) : Spot.K {
        val newSpot = kSpot - midSpot
        val vector = doubleArrayOf(newSpot.latitude, newSpot.longitude)

        val rotated = rotateVector(vector, angle)

        return Spot.K(rotated[0], rotated[1]) + midSpot
    }
}