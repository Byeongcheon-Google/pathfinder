package com.bcgg.visualizer

import androidx.compose.foundation.background
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.bcgg.Main
import com.bcgg.Spot

const val WINDOW_WIDTH = 720
const val WINDOW_HEIGHT = 720
const val ZOOM = 60

val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Cyan, Color.Magenta)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = WINDOW_WIDTH.dp, height = WINDOW_HEIGHT.dp)
    ) {
        var result by remember { mutableStateOf<Map<Spot, List<Spot>>>(mapOf()) }

        LaunchedEffect(Unit) {
            val spots = listOf(
                Spot(37.143390338353605, 127.19934255270758),
                Spot(36.58502231594391, 126.80008658020995),
                Spot(36.146410752746114, 126.91299467400488),
                Spot(37.3948131568412, 126.44890714976178),
                Spot(37.13637559501736, 126.22758669226063),
                Spot(38.13697559501736, 126.22458669226063),
                Spot(39.248758532292, 126.68221296973086),
                Spot(39.86328711961208, 126.30278396656459),
                Spot(39.921837822497544, 127.24296170070714),
                Spot(39.62395236398993, 126.78309104541616),
                Spot(39.66054391049293, 126.14327542490577)
            )

            result = Main.generateGroup(spots)
        }

        MaterialTheme {
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                var colorIndex = 0
                result.forEach { (kSpot, spots) ->
                    val color = colors[colorIndex++ % colors.size]
                    KSpot(color, kSpot)
                    spots.map { Spot(color, it) }
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun KSpot(color: Color, spot: Spot) {
    var active by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(bottom = spot.zoomedLatitude.dp, start = spot.zoomedLongitude.dp, top = 0.dp, end = 0.dp)
            .alpha(0.5f)
            .size(16.dp)
            .clip(CircleShape)
            .background(color)
            .onPointerEvent(PointerEventType.Enter) { active = true }
            .onPointerEvent(PointerEventType.Exit) { active = false }
    )
    if(active) {
        Text("K-Spot (${spot.latitude}, ${spot.longitude})")
    }

}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Spot(color: Color, spot: Spot) {
    var active by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(bottom = spot.zoomedLatitude.dp, start = spot.zoomedLongitude.dp, top = 0.dp, end = 0.dp)
            .size(8.dp)
            .clip(MaterialTheme.shapes.large)
            .background(color)
            .onPointerEvent(PointerEventType.Enter) { active = true }
            .onPointerEvent(PointerEventType.Exit) { active = false }
    )
    if(active) {
        Text("Spot (${spot.latitude}, ${spot.longitude})")
    }
}

inline val Spot.zoomedLatitude get() = (latitude - 32) * ZOOM
inline val Spot.zoomedLongitude get() = (longitude - 121) * ZOOM