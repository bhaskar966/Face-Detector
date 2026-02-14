package com.bhaskar.facedetector.presentation.faceDetection.ui_components

import android.graphics.Bitmap
import android.graphics.Rect
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.camera.viewfinder.core.ImplementationMode
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bhaskar.facedetector.presentation.faceDetection.components.FaceDetectionUiEvent
import com.bhaskar.facedetector.presentation.faceDetection.utils.processFaceDetection
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.Executors

@Composable
fun CameraPreviewWithDetection(
    cameraSelector: CameraSelector,
    onCameraReady: () -> Unit,
    onCameraError: (String) -> Unit,
    onFaceDetected: (Rect, Int, Int) -> Unit,
    onNoFaceDetected: () -> Unit,
    onPhotoTaken: (Bitmap, Float) -> Unit,
    eventChannel: Flow<FaceDetectionUiEvent>
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val surfaceRequest = remember { mutableStateOf<SurfaceRequest?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        eventChannel.collect { event ->
            if (event is FaceDetectionUiEvent.TriggerPhotoCapture) {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            onPhotoTaken(bitmap, image.imageInfo.rotationDegrees.toFloat())
                            image.close()
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onCameraError("Photo capture failed: ${exception.message}")
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(cameraSelector) {
        try {
            val cameraProvider = ProcessCameraProvider.awaitInstance(context)

            cameraProvider.unbindAll()

            val preview = Preview.Builder()
                .build()
                .apply {
                    setSurfaceProvider { request ->
                        surfaceRequest.value = request
                    }
                }

            // Image Analysis for face detection
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .apply {
                    setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                        processFaceDetection(
                            imageProxy = imageProxy,
                            onFaceDetected = onFaceDetected,
                            onNoFaceDetected = onNoFaceDetected
                        )
                    }
                }

            // Image Capture use case
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalysis,
                imageCapture
            )

            onCameraReady()

        } catch (e: Exception) {
            onCameraError(e.message ?: "Camera initialization failed")
        }
    }

    // Display camera preview
    surfaceRequest.value?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier.fillMaxSize(),
            implementationMode = ImplementationMode.EXTERNAL
        )
    }
}
