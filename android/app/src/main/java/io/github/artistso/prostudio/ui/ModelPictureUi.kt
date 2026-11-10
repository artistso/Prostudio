package io.github.artistso.prostudio.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.artistso.prostudio.canvas.CanvasViewModel

/** How wide a button on the bar for a picture being placed is. */
private val PictureButton = 88.dp

/**
 * While a picture is being placed on a model: the box round it, kept up with its place on the
 * window, and a bar to flip it, turn it, put it back, and keep it or take it away. A touch on the
 * picture moves it and two fingers on it scale and turn it; off it, a finger turns the model.
 */
@Composable
fun ModelPicture(vm: CanvasViewModel) {
    val corners = vm.modelPictureBox ?: return
    LaunchedEffect(Unit) {
        while (true) {
            withFrameNanos { }
            vm.readModelPicture()
        }
    }
    Box(Modifier.fillMaxSize()) {
        Canvas(Modifier.fillMaxSize()) {
            val path = Path()
            corners.chunked(2).forEachIndexed { i, (x, y) ->
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()
            drawPath(path, Color.Black.copy(alpha = 0.5f), style = Stroke(width = 4.dp.toPx()))
            drawPath(path, Color.White, style = Stroke(width = 2.dp.toPx()))
            corners.chunked(2).forEach { (x, y) -> drawCircle(Color.White, 6.dp.toPx(), Offset(x, y)) }
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp).clip(RoundedCornerShape(12.dp))
                .background(Ps.Panel).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ToolbarButton(PsIcons.FlipHorizontal, "Flip Horizontal", width = PictureButton, iconSize = 50.dp) { vm.flipModelPicture(true) }
            ToolbarButton(PsIcons.FlipVertical, "Flip Vertical", width = PictureButton, iconSize = 50.dp) { vm.flipModelPicture(false) }
            ToolbarButton(PsIcons.Rotate45, "Rotate 45°", width = PictureButton, iconSize = 44.dp) { vm.turnModelPicture() }
            ToolbarButton(PsIcons.TransformReset, "Reset", width = PictureButton, iconSize = 44.dp) { vm.resetModelPicture() }
            ToolbarButton(PsIcons.Cross, "Cancel", width = PictureButton, iconSize = 28.dp) { vm.endModelPicture(keep = false) }
            ToolbarButton(PsIcons.Tick, "Done", width = PictureButton, iconSize = 28.dp) { vm.endModelPicture(keep = true) }
        }
    }
}
