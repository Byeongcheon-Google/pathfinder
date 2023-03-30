package com.bcgg

import com.bcgg.pathgenerator.model.Spot
import com.bcgg.pathgenerator.PathGenerator

fun main() {
    val spots = listOf(
            Spot.Tour(37.143390338353605, 127.19934255270758),
            Spot.Tour(36.58502231594391, 126.80008658020995),
            Spot.Tour(36.146410752746114, 126.91299467400488),
            Spot.Tour(37.3948131568412, 126.44890714976178),
            Spot.Tour(37.13637559501736, 126.22758669226063),
            Spot.Tour(37.23637559501736, 126.22758669226063),
            Spot.Tour(39.248758532292, 126.68221296973086),
            Spot.Tour(39.86328711961208, 126.30278396656459),
            Spot.Tour(39.921837822497544, 127.24296170070714),
            Spot.Tour(39.62395236398993, 126.78309104541616),
            Spot.Tour(39.66054391049293, 126.14327542490577)
    )

    PathGenerator.generateGroup(spots)
}