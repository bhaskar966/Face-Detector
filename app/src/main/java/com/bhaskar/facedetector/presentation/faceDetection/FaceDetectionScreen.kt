package com.bhaskar.facedetector.presentation.faceDetection

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiEvent
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiEvents
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiState
import com.bhaskar.facedetector.presentation.faceDetection.ui_components.ErrorView
import com.bhaskar.facedetector.presentation.faceDetection.ui_components.FaceOvalOverlay
import com.bhaskar.facedetector.presentation.faceDetection.ui_components.PhotoPreviewDialog
import com.bhaskar.facedetector.presentation.faceDetection.ui_components.CameraPreviewWithDetection
import kotlinx.coroutines.flow.Flow

@Composable
fun FaceDetectionScreen(
    faceDetectionUiState: FaceDetectionUiState,
    onEvent: (FaceDetectionUiEvents) -> Unit,
    eventChannel: Flow<FaceDetectionUiEvent>,
    onPhotoCaptured: (Bitmap) -> Unit,
    onBackPressed: () -> Unit
) {

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        eventChannel.collect { event ->
            when (event) {
                is FaceDetectionUiEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                is FaceDetectionUiEvent.NavigateBackWithResult -> {
                    onPhotoCaptured(event.bitmap)
                }
                is FaceDetectionUiEvent.TriggerPhotoCapture -> {
                    // This is handled in the camera component
                }
            }
        }
    }

    FaceDetectionContent(
        uiState = faceDetectionUiState,
        onEvent = onEvent,
        onBackPressed = onBackPressed,
        eventChannel = eventChannel
    )
}

@Composable
fun FaceDetectionContent(
    uiState: FaceDetectionUiState,
    onEvent: (FaceDetectionUiEvents) -> Unit,
    onBackPressed: () -> Unit,
    eventChannel: Flow<FaceDetectionUiEvent>
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview with face detection
        if (uiState.isCameraReady || uiState.isLoading) {
            CameraPreviewWithDetection(
                cameraSelector = uiState.cameraSelector,
                onCameraReady = { onEvent(FaceDetectionUiEvents.OnCameraReady) },
                onCameraError = { error -> onEvent(FaceDetectionUiEvents.OnCameraError(error)) },
                onFaceDetected = { boundingBox, imageWidth, imageHeight ->
                    onEvent(
                        FaceDetectionUiEvents.OnFaceDetected(
                            faceBoundingBox = boundingBox,
                            imageWidth = imageWidth,
                            imageHeight = imageHeight
                        )
                    )
                },
                onNoFaceDetected = { onEvent(FaceDetectionUiEvents.OnNoFaceDetected) },
                onPhotoTaken = { bitmap, rotation ->
                    onEvent(
                        FaceDetectionUiEvents.OnPhotoTaken(bitmap, rotation)
                    )
                },
                eventChannel = eventChannel
            )
        }

        // Oval overlay
        FaceOvalOverlay(
            modifier = Modifier.fillMaxSize(),
            ovalColor = uiState.ovalColor,
            onDimensionsChanged = { width, height, ovalBounds ->
                onEvent(
                    FaceDetectionUiEvents.OnViewDimensionsChanged(
                        viewWidth = width,
                        viewHeight = height,
                        ovalBounds = ovalBounds
                    )
                )
            }
        )

        // Top bar with back button
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Bottom controls
        if (uiState.isCameraReady) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Flip camera button
                IconButton(
                    onClick = { onEvent(FaceDetectionUiEvents.OnFlipCamera) },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FlipCameraAndroid,
                        contentDescription = "Flip camera",
                        tint = Color.White
                    )
                }

                // Capture button
                IconButton(
                    onClick = { onEvent(FaceDetectionUiEvents.OnCapturePhoto) },
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = Color.White,
                            shape = CircleShape
                        )
                        .border(
                            width = 4.dp,
                            color = Color.Black.copy(alpha = 0.5f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Camera,
                        contentDescription = "Capture photo",
                        tint = Color.Black,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.size(56.dp))
            }
        }

        // Loading indicator
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }

        // Error state
        if (uiState.errorMessage != null) {
            ErrorView(
                message = uiState.errorMessage,
                onRetry = { onEvent(FaceDetectionUiEvents.OnRetryCamera) }
            )
        }

        // Preview dialog
        if (uiState.showPreviewDialog && uiState.capturedImageBitmap != null) {
            PhotoPreviewDialog(
                bitmap = uiState.capturedImageBitmap,
                onConfirm = { onEvent(FaceDetectionUiEvents.OnConfirmPhoto) },
                onRetake = { onEvent(FaceDetectionUiEvents.OnRetakePhoto) }
            )
        }
    }
}