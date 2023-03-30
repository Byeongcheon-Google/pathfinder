package com.bcgg.visualizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotateRad
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.bcgg.pathgenerator.PathGenerator
import com.bcgg.util.RandomCoordinatesGenerator
import com.bcgg.pathgenerator.model.Spot
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

const val WINDOW_WIDTH = 720
const val WINDOW_HEIGHT = 720
const val ZOOM = 60

val colors =
    listOf(Color.Red, Color.Blue, Color.Green, Color.Cyan, Color.Magenta, Color.Gray, Color.Yellow, Color.Black)

@OptIn(ExperimentalComposeUiApi::class, ExperimentalFoundationApi::class)
fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Compose for Desktop",
        state = rememberWindowState(width = WINDOW_WIDTH.dp, height = WINDOW_HEIGHT.dp)
    ) {
        var latOffset by remember { mutableStateOf(32.0) }
        var lonOffset by remember { mutableStateOf(121.0) }
        var zoom by remember { mutableStateOf(40.0) }
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
        var result by remember {
            mutableStateOf<List<Pair<Spot.K, List<Spot>>>>(listOf())
        }

        LaunchedEffect(spots) {
            result = PathGenerator.generateGroup(spots).toList()
        }

        MaterialTheme {
            Column {
                Button(onClick = {
                    spots = RandomCoordinatesGenerator.generate(15)
                }) {
                    Text("Refresh")
                }

                Text(String.format("offset = %7f, %7f", latOffset, lonOffset))
                Text("${zoom.roundToInt()}x")
            }

            /*Box(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                var colorIndex = 0
                result.forEach { (kSpot, spots) ->
                    val color = colors[colorIndex++ % colors.size]
                    Spot(color, kSpot, latOffset, lonOffset, zoom)
                    spots.map { Spot(color, it, latOffset, lonOffset, zoom) }
                }
            }*/

            MapCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .onDrag {
                        lonOffset -= it.x / zoom
                        latOffset += it.y / zoom
                    }
                    .onPointerEvent(PointerEventType.Scroll) {
                        zoom += it.changes.first().scrollDelta.y
                    },
                latOffset = latOffset,
                lonOffset = lonOffset,
                zoom = zoom,
                path = result
            )
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun MapCanvas(
    modifier: Modifier = Modifier,
    latOffset: Double,
    lonOffset: Double,
    zoom: Double,
    path: List<Pair<Spot.K, Collection<Spot>>>,
) {
    var width by remember { mutableStateOf(0) }
    var height by remember { mutableStateOf(0) }
    val textMeasurer = rememberTextMeasurer()
    val textStyle = LocalTextStyle.current
    Canvas(modifier.onGloballyPositioned {
        width = it.size.width
        height = it.size.height
    }) {
        val kSpotRadius = 10.dp.toPx()
        val spotRadius = 5.dp.toPx()
        var previousKSpot: Spot.K? = null

        path.mapIndexed { i, (kSpot, spots) ->

            val kSpotX = kSpot.guiLongitude(lonOffset, zoom).dp.toPx()
            val kSpotY = (height - kSpot.guiLatitude(latOffset, zoom)).dp.toPx()

            // Draw line
            if (previousKSpot != null) {
                val prevKSpotX = previousKSpot!!.guiLongitude(lonOffset, zoom).dp.toPx()
                val prevKSpotY = (height - previousKSpot!!.guiLatitude(latOffset, zoom)).dp.toPx()

                drawLine(
                    color = Color.Black,
                    start = Offset(prevKSpotX, prevKSpotY),
                    end = Offset(kSpotX, kSpotY),
                )
            }

            // Draw K Spot
            drawCircle(
                color = colors[i % colors.size].copy(alpha = 0.1f),
                center = Offset(kSpotX, kSpotY),
                radius = kSpotRadius
            )

            val measure = textMeasurer.measure(AnnotatedString("${i + 1}"), style = textStyle)
            if (measure.size.width > 0 && measure.size.height > 0) {
                drawText(
                    textLayoutResult = measure,
                    topLeft = Offset(kSpotX, kSpotY)
                )
            }


            //Draw spot
            spots.map {
                val spotX = it.guiLongitude(lonOffset, zoom).dp.toPx()
                val spotY = (height - it.guiLatitude(latOffset, zoom)).dp.toPx()

                drawCircle(
                    color = colors[i % colors.size].copy(alpha = 0.35f),
                    center = Offset(spotX, spotY),
                    radius = spotRadius
                )
            }

            previousKSpot = kSpot
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Spot(color: Color, spot: Spot, latOffset: Double, lonOffset: Double, zoom: Double) {
    var active by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .padding(
                bottom = spot.guiLatitude(latOffset, zoom).dp,
                start = spot.guiLongitude(lonOffset, zoom).dp,
                top = 0.dp,
                end = 0.dp
            )
            .size(if (spot is Spot.K) 16.dp else 8.dp)
            .alpha(if (spot is Spot.K) 0.5f else 1f)
            .clip(
                when (spot) {
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

fun Spot.guiLatitude(latOffset: Double, zoom: Double): Float = ((latitude - latOffset) * zoom).toFloat()
fun Spot.guiLongitude(lonOffset: Double, zoom: Double): Float = ((longitude - lonOffset) * zoom).toFloat()