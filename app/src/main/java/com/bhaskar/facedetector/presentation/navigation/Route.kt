package com.bhaskar.facedetector.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Route {

    @Serializable
    data object MainScreen: Route()

    @Serializable
    data object FaceDetectionScreen: Route()


}