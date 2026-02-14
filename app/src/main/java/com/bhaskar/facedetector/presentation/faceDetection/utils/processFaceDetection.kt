package com.bhaskar.facedetector.presentation.faceDetection.utils

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions

@androidx.annotation.OptIn(ExperimentalGetImage::class)
fun processFaceDetection(
    imageProxy: ImageProxy,
    onFaceDetected: (Rect, Int, Int) -> Unit,
    onNoFaceDetected: () -> Unit
) {
    val mediaImage = imageProxy.image
    if (mediaImage != null) {
        val image = InputImage.fromMediaImage(
            mediaImage,
            imageProxy.imageInfo.rotationDegrees
        )

        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
            .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
            .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
            .setMinFaceSize(0.15f)
            .build()

        val faceDetector = FaceDetection.getClient(options)

        faceDetector.process(image)
            .addOnSuccessListener { faces ->
                if (faces.isNotEmpty()) {
                    val face = faces[0]
                    onFaceDetected(
                        face.boundingBox,
                        imageProxy.width,
                        imageProxy.height
                    )
                } else {
                    onNoFaceDetected()
                }
            }
            .addOnFailureListener { e ->
                Log.e("FaceDetection", "Detection failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    } else {
        imageProxy.close()
    }
}