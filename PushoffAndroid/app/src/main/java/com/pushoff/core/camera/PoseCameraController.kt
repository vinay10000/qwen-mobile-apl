package com.pushoff.core.camera

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import com.google.mediapipe.framework.image.MPImageBuilder
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * CameraX integration with MediaPipe Pose Landmarker.
 * Provides real-time pose detection for push-up counting.
 */
class PoseCameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val onPoseDetected: (PoseLandmarkerResult) -> Unit
) {
    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var preview: Preview? = null
    private var imageAnalyzer: ImageAnalysis? = null
    private var poseLandmarker: PoseLandmarker? = null
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    
    // MediaPipe Pose Landmarker options
    private fun createPoseLandmarker(): PoseLandmarker {
        return PoseLandmarker.createFromOptions(
            context,
            PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions {
                    it.setModelAssetPath("pose_landmarker_full.task")
                }
                .setRunningMode(PoseLandmarker.RunningMode.LIVE_STREAM)
                .setResultListener(this::processPoseResult)
                .setErrorListener { error -> 
                    println("Pose landmarker error: $error")
                }
                .build()
        )
    }
    
    private fun processPoseResult(result: PoseLandmarkerResult, image: android.media.Image) {
        onPoseDetected(result)
        image.close()
    }
    
    fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                // Preview use case
                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                
                // Image analysis use case for pose detection
                imageAnalyzer = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            analyzeImage(imageProxy)
                        }
                    }
                
                // Select back camera
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                
                // Unbind all use cases before rebinding
                cameraProvider?.unbindAll()
                
                // Bind use cases to camera
                camera = cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                
                // Initialize pose landmarker
                poseLandmarker = createPoseLandmarker()
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, cameraExecutor)
    }
    
    private fun analyzeImage(imageProxy: androidx.camera.core.ImageProxy) {
        val bitmap = android.graphics.Bitmap.createBitmap(
            imageProxy.width,
            imageProxy.height,
            android.graphics.Bitmap.Config.ARGB_8888
        )
        
        val buffer = imageProxy.planes[0].buffer
        bitmap.copyPixelsFromBuffer(buffer)
        
        // Convert to MediaPipe Image for pose detection
        val mpImage = MPImageBuilder(bitmap).build()
        
        // Send to pose landmarker
        poseLandmarker?.detectAsync(mpImage, System.currentTimeMillis())
        
        imageProxy.close()
    }
    
    fun stopCamera() {
        cameraProvider?.unbindAll()
        poseLandmarker?.close()
        cameraExecutor.shutdown()
    }
    
    fun toggleCamera() {
        // Switch between front and back camera
        val currentCamera = camera
        if (currentCamera != null) {
            val cameraSelector = if (currentCamera.cameraInfo.lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }
            
            cameraProvider?.unbindAll()
            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        }
    }
}
