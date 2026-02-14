package com.bhaskar.facedetector.presentation.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bhaskar.facedetector.domin.usecase.DetectFacePositionUseCase
import com.bhaskar.facedetector.presentation.faceDetection.FaceDetectionScreen
import com.bhaskar.facedetector.presentation.faceDetection.FaceDetectionViewModel
import com.bhaskar.facedetector.presentation.main.MainScreen
import com.bhaskar.facedetector.presentation.main.MainViewModel
import com.bhaskar.facedetector.presentation.main.components.MainScreenEvent

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    hasCameraPermission: Boolean,
    paddingValues: PaddingValues
) {

    val mainViewModel: MainViewModel = viewModel()
    val mainUiState by mainViewModel.uiState.collectAsState()
    val mainUiEvent = mainViewModel::onEvent

    NavHost(
        navController = navController,
        startDestination = Route.MainScreen
    ) {
        composable<Route.MainScreen> {
            MainScreen(
                mainScreenUiState = mainUiState,
                onEvent = mainUiEvent,
                hasCameraPermission = hasCameraPermission,
                onOpenCamera = {
                    navController.navigate(Route.FaceDetectionScreen)
                },
            )
        }

        composable<Route.FaceDetectionScreen> {

            val facePos = DetectFacePositionUseCase()
            val faceDetectionViewModel: FaceDetectionViewModel = viewModel(
                factory = viewModelFactory {
                    initializer {
                        FaceDetectionViewModel(
                            detectFacePositionUseCase = facePos
                        )
                    }
                }
            )
            val faceDetectionUiState by faceDetectionViewModel.faceDetectionUiState.collectAsState()
            val faceDetectionUiEvent = faceDetectionViewModel::onEvent

            FaceDetectionScreen(
                faceDetectionUiState = faceDetectionUiState,
                onEvent = faceDetectionUiEvent,
                eventChannel = faceDetectionViewModel.eventChannel,
                onPhotoCaptured = { bitmap ->
                    mainViewModel.onEvent(MainScreenEvent.OnImageCaptured(bitmap))
                    navController.navigateUp()
                },
                onBackPressed = {
                    navController.navigateUp()
                },
                paddingValues = paddingValues
            )
        }
    }
}