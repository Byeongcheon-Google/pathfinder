package com.bcgg.visualizer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.bcgg.pathgenerator.PathGenerator
import com.bcgg.util.RandomCoordinatesGenerator
import com.bcgg.pathgenerator.model.Spot

const val WINDOW_WIDTH = 720
const val WINDOW_HEIGHT = 720
const val ZOOM = 40

val colors = listOf(Color.Red, Color.Blue, Color.Green, Color.Cyan, Color.Magenta)

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = WINDOW_WIDTH.dp, height = WINDOW_HEIGHT.dp)
    ) {
        var spots by remember {
            mutableStateOf(
                listOf(
                    Spot.Tour(37.143390338353605, 127.19934255270758),
                    Spot.Food(36.58502231594391, 126.80008658020995),
                    Spot.Tour(36.146410752746114, 126.91299467400488),
                    Spot.Tour(37.3948131568412, 126.44890714976178),
                    Spot.Tour(37.13637559501736, 126.22758669226063),
                    Spot.Tour(37.23637559501736, 126.22758669226063),
                    Spot.Tour(39.248758532292, 126.68221296973086),
                    Spot.Food(39.86328711961208, 126.30278396656459),
                    Spot.Tour(39.921837822497544, 127.24296170070714),
                    Spot.Tour(39.62395236398993, 126.78309104541616),
                    Spot.Tour(39.66054391049293, 126.14327542490577)
                )
            )
        }
        var result by remember { mutableStateOf<Map<Spot.K, List<Spot>>>(mapOf()) }

        LaunchedEffect(spots) {
            result = PathGenerator.generateGroup(spots)
        }

        MaterialTheme {
            Button(onClick = {
                spots = RandomCoordinatesGenerator.generate(15)
            }) {
                Text("Refresh")
            }
            Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                var colorIndex = 0
                result.forEach { (kSpot, spots) ->
                    val color = colors[colorIndex++ % colors.size]
                    Spot(color, kSpot)
                    spots.map { Spot(color, it) }
                }
                Spot(Color.Black, Spot.House(35.143390338353605, 124.19934255270758))
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Spot(color: Color, spot: Spot) {
    var active by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(bottom = spot.zoomedLatitude.dp, start = spot.zoomedLongitude.dp, top = 0.dp, end = 0.dp)
            .size(if(spot is Spot.K) 16.dp else 8.dp)
            .alpha(if(spot is Spot.K) 0.5f else 1f)
            .clip(
                when(spot) {
                    is Spot.Food -> CutCornerShape(4.dp)
                    is Spot.House -> RoundedCornerShape(4.dp)
                    is Spot.Tour -> RoundedCornerShape(0.dp)
                    is Spot.K -> CircleShape
                    else -> RoundedCornerShape(0.dp)
                }
            )
            .background(color)
            .onPointerEvent(PointerEventType.Enter) { active = true }
            .onPointerEvent(PointerEventType.Exit) { active = false }
    )
    if (active) {
        Text("${spot.javaClass.name} (${spot.latitude}, ${spot.longitude})")
    }
}

inline val Spot.zoomedLatitude get() = (latitude - 32) * ZOOM
inline val Spot.zoomedLongitude get() = (longitude - 121) * ZOOM