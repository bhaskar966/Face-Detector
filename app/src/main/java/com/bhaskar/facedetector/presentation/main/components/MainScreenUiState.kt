package com.bhaskar.facedetector.presentation.main.components

import android.graphics.Bitmap

data class MainScreenUiState(
    val capturedImageBitmap: Bitmap? = null,
    val isLoading: Boolean = false
)