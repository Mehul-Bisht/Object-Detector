package com.example.objectdetector

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.objectdetector.DetectorViewModel.DetectedState
import com.example.objectdetector.databinding.ActivityDetectorBinding
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class DetectorActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            listOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).toTypedArray()
    }

    private lateinit var binding: ActivityDetectorBinding
    private val detectorViewModel: DetectorViewModel by viewModels()
    private val executor = Executors.newSingleThreadExecutor()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (allPermissionsGranted()) {
            startCamera()
            listenToTTS()
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
                listenToTTS()
            } else {
                Toast.makeText(
                    this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("UnsafeOptInUsageError")
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(
                executor,
                ImageAnalysis.Analyzer { imageProxy ->
                    val mediaImage = imageProxy.image

                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )
                        val imageLabeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
                        imageLabeler.process(image).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                task.result.forEach { imageLabel ->
                                    detectorViewModel.submitLabel(imageLabel)
                                }
                            } else {
                                task.exception?.printStackTrace()
                            }
                            imageProxy.close()
                        }
                    }
                }
            )

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    imageAnalysis,
                    preview
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun listenToTTS() {
        detectorViewModel.initTTS(this)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                detectorViewModel.detectedObjects.collectLatest { state ->
                    when(state) {
                        is DetectedState.Initial -> Unit
                        is DetectedState.Success -> {
                            val firstDetection = state.data
                            Log.d("Detection ", firstDetection)
                            detectorViewModel.speakTTS(firstDetection)
                            binding.recognisedText.text = firstDetection
                        }
                        is DetectedState.Error -> {
                            detectorViewModel.speakTTS(state.msg)
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        detectorViewModel.stopTTS()
    }

    override fun onDestroy() {
        detectorViewModel.stopTTS()
        detectorViewModel.shutdownTTS()
        super.onDestroy()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }
}