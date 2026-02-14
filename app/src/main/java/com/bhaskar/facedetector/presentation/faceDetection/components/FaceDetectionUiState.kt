package com.bhaskar.facedetector.presentation.faceDetection.components

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
import com.bhaskar.facedetector.domin.model.FaceDetectionResult
import com.bhaskar.facedetector.domin.model.OvalColor

data class FaceDetectionUiState(
    val isLoading: Boolean = true,
    val isCameraReady: Boolean = false,
    val faceDetectionResult: FaceDetectionResult? = null,
    val ovalColor: OvalColor = OvalColor.OvalNeutral,
    val cameraSelector: CameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA,
    val capturedImageBitmap: Bitmap? = null,
    val showPreviewDialog: Boolean = false,
    val errorMessage: String? = null
)