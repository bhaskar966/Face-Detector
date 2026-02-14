package com.bhaskar.facedetector.domin.usecase

import android.graphics.Rect
import android.graphics.RectF
import com.bhaskar.facedetector.domin.model.FaceDetectionResult
import kotlin.math.pow

class DetectFacePositionUseCase {

    operator fun invoke(
        faceBoundingBox: Rect,
        ovalBounds: RectF,
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
    ): FaceDetectionResult {

        val transformedBox = transformBoundingBox(
            box = faceBoundingBox,
            imageWidth = imageWidth,
            imageHeight = imageHeight,
            viewWidth = viewWidth,
            viewHeight = viewHeight
        )

        val isInside = checkIfFaceInOval(
            faceBox = transformedBox,
            ovalBounds = ovalBounds
        )

        return FaceDetectionResult(
            boundingBox = transformedBox,
            isInsideOval = isInside,
            confidence = 1.0f
        )

    }


    private fun transformBoundingBox(
        box: Rect,
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int,
    ): Rect {

        val scaleX = viewWidth.toFloat() / imageWidth
        val scaleY = viewHeight.toFloat() / imageHeight

        return Rect(
            (box.left * scaleX).toInt(),
            (box.top * scaleY).toInt(),
            (box.right * scaleX).toInt(),
            (box.bottom * scaleY).toInt(),
        )

    }

    private fun checkIfFaceInOval(
        faceBox: Rect,
        ovalBounds: RectF
    ): Boolean {

        val faceCenterX = faceBox.exactCenterX()
        val faceCenterY = faceBox.exactCenterY()

        val ovalCenterX = ovalBounds.centerX()
        val ovalCenterY = ovalBounds.centerY()
        val radiusX = ovalBounds.width() / 2
        val radiusY = ovalBounds.height() / 2

        // Ellipse equation: (x-cx)^2/rx^2 + (y-cy)^2/ry^2 <= 1
        val normalized = ((faceCenterX - ovalCenterX) / radiusX).toDouble().pow(2.0) + ((faceCenterY - ovalCenterY) / radiusY).toDouble().pow(2.0)

        return normalized <= 0.9

    }


}