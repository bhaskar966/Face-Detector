package com.bhaskar.facedetector.presentation.faceDetection.ui_components

import android.graphics.RectF
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.bhaskar.facedetector.domin.model.OvalColor

@Composable
fun FaceOvalOverlay(
    modifier: Modifier = Modifier,
    ovalColor: OvalColor,
    onDimensionsChanged: (Int, Int, RectF) -> Unit
) {
    Canvas(
        modifier = modifier.onSizeChanged { size ->
            val minDimension = minOf(size.width, size.height).toFloat()

            val centerX = size.width / 2f
            val centerY = size.height / 2f

            val ovalWidth = minDimension * 0.60f
            val ovalHeight = minDimension * 0.80f

            val ovalBounds = RectF(
                centerX - ovalWidth / 2,
                centerY - ovalHeight / 2,
                centerX + ovalWidth / 2,
                centerY + ovalHeight / 2
            )

            onDimensionsChanged(size.width, size.height, ovalBounds)
        }
    ) {
        val minDimension = minOf(size.width, size.height)

        val centerX = size.width / 2
        val centerY = size.height / 2

        val ovalWidth = minDimension * 0.60f
        val ovalHeight = minDimension * 0.80f

        drawOval(
            color = ovalColor.color,
            topLeft = Offset(
                centerX - ovalWidth / 2,
                centerY - ovalHeight / 2
            ),
            size = Size(ovalWidth, ovalHeight),
            style = Stroke(width = 8.dp.toPx())
        )
    }
}
