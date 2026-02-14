package com.bhaskar.facedetector.presentation.main

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.bhaskar.facedetector.presentation.main.components.MainScreenEvent
import com.bhaskar.facedetector.presentation.main.components.MainScreenUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MainViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(MainScreenUiState())
    val uiState: StateFlow<MainScreenUiState> = _uiState.asStateFlow()

    fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.OnImageCaptured -> handleImageCaptured(event.bitmap)
            is MainScreenEvent.OnDeleteImage -> handleDeleteImage()
        }
    }

    private fun handleImageCaptured(bitmap: Bitmap) {
        _uiState.update { it.copy(capturedImageBitmap = bitmap) }
    }

    private fun handleDeleteImage() {
        _uiState.update { it.copy(capturedImageBitmap = null) }
    }
}