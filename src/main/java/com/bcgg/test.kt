package com.bcgg

fun main() {
    val spots = listOf(
            Spot(37.143390338353605, 127.19934255270758),
            Spot(36.58502231594391, 126.80008658020995),
            Spot(36.146410752746114, 126.91299467400488),
            Spot(37.3948131568412, 126.44890714976178),
            Spot(37.13637559501736, 126.22758669226063),
            Spot(39.248758532292, 126.68221296973086),
            Spot(39.86328711961208, 126.30278396656459),
            Spot(39.921837822497544, 127.24296170070714),
            Spot(39.62395236398993, 126.78309104541616),
            Spot(39.66054391049293, 126.14327542490577)
    )

    Main.generateGroup(spots)
}