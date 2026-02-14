package com.bhaskar.facedetector.domin.model

import android.graphics.Rect


data class FaceDetectionResult(
    val boundingBox: Rect,
    val isInsideOval: Boolean,
    val confidence: Float
)
