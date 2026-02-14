package com.bhaskar.facedetector.presentation.main.components

import android.graphics.Bitmap

sealed class MainScreenEvent {
    data class OnImageCaptured(val bitmap: Bitmap) : MainScreenEvent()
    object OnDeleteImage : MainScreenEvent()
}