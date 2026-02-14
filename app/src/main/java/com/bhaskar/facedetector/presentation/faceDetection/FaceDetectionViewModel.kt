package com.bhaskar.facedetector.presentation.faceDetection

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.CameraSelector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bhaskar.facedetector.domin.model.OvalColor
import com.bhaskar.facedetector.domin.model.ViewDimensions
import com.bhaskar.facedetector.domin.usecase.DetectFacePositionUseCase
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiEvent
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiEvents
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FaceDetectionViewModel(
    private val detectFacePositionUseCase: DetectFacePositionUseCase,
): ViewModel() {

    private val _faceDetectionUiState = MutableStateFlow(FaceDetectionUiState())
    val faceDetectionUiState = _faceDetectionUiState.asStateFlow()

    private var viewDimensions: ViewDimensions? = null

    // SharedFlow for one-time events
    private val _eventChannel = MutableSharedFlow<FaceDetectionUiEvent>()
    val eventChannel = _eventChannel.asSharedFlow()

    fun onEvent(event: FaceDetectionUiEvents) {
        when(event){
            is FaceDetectionUiEvents.OnCameraReady -> handleCameraReady()
            is FaceDetectionUiEvents.OnCameraError -> handleCameraError(event.error)
            is FaceDetectionUiEvents.OnFaceDetected -> handleFaceDetected(event)
            is FaceDetectionUiEvents.OnNoFaceDetected -> handleNoFaceDetected()
            is FaceDetectionUiEvents.OnViewDimensionsChanged -> handleViewDimensionsChanged(event)
            is FaceDetectionUiEvents.OnRetryCamera -> handleRetryCamera()
            is FaceDetectionUiEvents.OnCapturePhoto -> handleCapturePhoto()
            is FaceDetectionUiEvents.OnFlipCamera -> handleFlipCamera()
            is FaceDetectionUiEvents.OnConfirmPhoto -> handleConfirmPhoto()
            is FaceDetectionUiEvents.OnRetakePhoto -> handleRetakePhoto()
            is FaceDetectionUiEvents.OnPhotoTaken -> onPhotoTaken(event.bitmap, event.rotation)
        }
    }

    private fun handleRetryCamera() {
        _faceDetectionUiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null
            )
        }
    }

    private fun handleViewDimensionsChanged(event: FaceDetectionUiEvents.OnViewDimensionsChanged) {
        viewDimensions = ViewDimensions(
            viewWidth = event.viewWidth,
            viewHeight = event.viewHeight,
            ovalBounds = event.ovalBounds
        )
    }

    private fun handleNoFaceDetected() {
        _faceDetectionUiState.update {
            it.copy(
                faceDetectionResult = null,
                ovalColor = OvalColor.OvalNeutral
            )
        }
    }

    private fun handleFaceDetected(event: FaceDetectionUiEvents.OnFaceDetected) {
        val dimensions = viewDimensions ?: return

        val result = detectFacePositionUseCase(
            faceBoundingBox = event.faceBoundingBox,
            ovalBounds = dimensions.ovalBounds,
            imageWidth = event.imageWidth,
            imageHeight = event.imageHeight,
            viewWidth = dimensions.viewWidth,
            viewHeight = dimensions.viewHeight
        )

        _faceDetectionUiState.update {
            it.copy(
                faceDetectionResult = result,
                ovalColor = when {
                    result.isInsideOval -> OvalColor.OvalSuccess
                    else -> OvalColor.OvalError
                }
            )
        }
    }

    private fun handleCameraError(error: String) {
        _faceDetectionUiState.update {
            it.copy(
                isLoading = false,
                isCameraReady = false,
                errorMessage = error
            )
        }
    }

    private fun handleCameraReady() {
        _faceDetectionUiState.update {
            it.copy(
                isLoading = false,
                isCameraReady = true,
                errorMessage = null
            )
        }
    }

    private fun handleCapturePhoto() {
        val currentState = _faceDetectionUiState.value

        if (currentState.faceDetectionResult?.isInsideOval == true) {
            viewModelScope.launch {
                _eventChannel.emit(FaceDetectionUiEvent.TriggerPhotoCapture)
            }
        } else {
            viewModelScope.launch {
                _eventChannel.emit(
                    FaceDetectionUiEvent.ShowToast("Please position your face inside the oval")
                )
            }
        }
    }

    fun onPhotoTaken(
        bitmap: Bitmap,
        rotation: Float
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val matrix = Matrix().apply {
                postRotate(rotation)
                // Flip the image when clicked from front camera - to mirror the image
                if (faceDetectionUiState.value.cameraSelector == CameraSelector.DEFAULT_FRONT_CAMERA){
                    postScale(-1f, 1f)
                }
            }

            val newBitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.width,
                bitmap.height,
                matrix,
                true
            )

            _faceDetectionUiState.update {
                it.copy(
                    capturedImageBitmap = newBitmap,
                    showPreviewDialog = true
                )
            }
        }
    }

    private fun handleFlipCamera() {
        val currentSelector = _faceDetectionUiState.value.cameraSelector
        val newSelector = if (currentSelector == CameraSelector.DEFAULT_FRONT_CAMERA) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        _faceDetectionUiState.update {
            it.copy(
                cameraSelector = newSelector,
                ovalColor = OvalColor.OvalNeutral,
                faceDetectionResult = null
            )
        }
    }

    private fun handleConfirmPhoto() {
        val bitmap = _faceDetectionUiState.value.capturedImageBitmap ?: return

        viewModelScope.launch {
            _eventChannel.emit(FaceDetectionUiEvent.NavigateBackWithResult(bitmap))
        }
    }

    private fun handleRetakePhoto() {
        _faceDetectionUiState.update {
            it.copy(
                capturedImageBitmap = null,
                showPreviewDialog = false,
                ovalColor = OvalColor.OvalNeutral
            )
        }
    }
}
