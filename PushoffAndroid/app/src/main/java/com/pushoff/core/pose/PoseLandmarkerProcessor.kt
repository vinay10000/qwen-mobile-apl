package com.pushoff.core.pose

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult

/**
 * MediaPipe Pose Landmarker processor for real-time pose detection.
 * Converts camera frames to pose landmarks for push-up detection.
 */
class PoseLandmarkerProcessor(
    context: Context,
    private val onPoseDetected: (PoseLandmarkerResult) -> Unit
) {
    private var poseLandmarker: PoseLandmarker? = null

    init {
        initializePoseLandmarker(context)
    }

    /**
     * Initialize the MediaPipe Pose Landmarker with the bundled model file.
     */
    private fun initializePoseLandmarker(context: Context) {
        try {
            val baseOptions = PoseLandmarker.PoseLandmarkerOptions.BaseOptions.builder()
                .setModelAssetPath("pose_landmarker_full.task")
                .build()

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener(this::handlePoseResult)
                .setErrorListener { error ->
                    println("PoseLandmarkerProcessor error: ${error.message}")
                }
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
            println("PoseLandmarker initialized successfully")
        } catch (e: Exception) {
            println("Failed to initialize PoseLandmarker: ${e.message}")
            e.printStackTrace()
        }
    }

    /**
     * Handle pose detection results from MediaPipe.
     */
    private fun handlePoseResult(result: PoseLandmarkerResult, image: android.media.Image) {
        // Forward the result to the callback
        onPoseDetected(result)
        image.close()
    }

    /**
     * Process a bitmap frame for pose detection.
     * This is called by the camera pipeline for each frame.
     */
    fun processFrame(bitmap: Bitmap, timestampMs: Long) {
        poseLandmarker?.let { landmarker ->
            try {
                val mpImage = com.google.mediapipe.framework.image.MPImageBuilder(bitmap).build()
                landmarker.detectAsync(mpImage, timestampMs)
            } catch (e: Exception) {
                println("Error processing frame: ${e.message}")
            }
        }
    }

    /**
     * Process an Android Media.Image for pose detection.
     * More efficient than Bitmap conversion when possible.
     */
    fun processMediaImage(image: android.media.Image, timestampMs: Long) {
        poseLandmarker?.let { landmarker ->
            try {
                val mpImage = com.google.mediapipe.framework.image.MPImageBuilder(image).build()
                landmarker.detectAsync(mpImage, timestampMs)
            } catch (e: Exception) {
                println("Error processing media image: ${e.message}")
                image.close()
            }
        }
    }

    /**
     * Clean up resources when done.
     */
    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
        println("PoseLandmarker closed")
    }

    /**
     * Check if the landmarker is ready for processing.
     */
    fun isReady(): Boolean = poseLandmarker != null
}
