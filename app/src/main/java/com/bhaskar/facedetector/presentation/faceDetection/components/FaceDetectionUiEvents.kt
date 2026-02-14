package com.bhaskar.facedetector.presentation.faceDetection.components

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF

sealed class FaceDetectionUiEvents {
    data object OnCameraReady : FaceDetectionUiEvents()
    data class OnCameraError(val error: String) : FaceDetectionUiEvents()

    data class OnFaceDetected(
        val faceBoundingBox: Rect,
        val imageWidth: Int,
        val imageHeight: Int
    ) : FaceDetectionUiEvents()

    data object OnNoFaceDetected : FaceDetectionUiEvents()

    data class OnViewDimensionsChanged(
        val viewWidth: Int,
        val viewHeight: Int,
        val ovalBounds: RectF
    ) : FaceDetectionUiEvents()

    data object OnRetryCamera : FaceDetectionUiEvents()
    data object OnCapturePhoto : FaceDetectionUiEvents()
    data object OnFlipCamera : FaceDetectionUiEvents()
    data object OnConfirmPhoto : FaceDetectionUiEvents()
    data object OnRetakePhoto : FaceDetectionUiEvents()
    data class OnPhotoTaken(val bitmap: Bitmap, val rotation: Float) : FaceDetectionUiEvents()
}

sealed class FaceDetectionUiEvent {
    data class ShowToast(val message: String) : FaceDetectionUiEvent()
    data object TriggerPhotoCapture : FaceDetectionUiEvent()
    data class NavigateBackWithResult(val bitmap: Bitmap) : FaceDetectionUiEvent()
}