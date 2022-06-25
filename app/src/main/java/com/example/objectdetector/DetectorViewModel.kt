package com.example.objectdetector

import android.content.Context
import android.speech.tts.TextToSpeech
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.common.collect.EvictingQueue
import com.google.mlkit.vision.label.ImageLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class DetectorViewModel : ViewModel() {

    private lateinit var tts: TextToSpeech

    private val _detectedObjects: MutableStateFlow<DetectedState> =
        MutableStateFlow(DetectedState.Initial)
    val detectedObjects: StateFlow<DetectedState> get() = _detectedObjects

    private val cache: EvictingQueue<String> = EvictingQueue.create(2)

    private var currentObject = ""

    sealed class DetectedState {
        object Initial : DetectedState()
        data class Success(val data: String) : DetectedState()
        data class Error(val msg: String) : DetectedState()
    }

    init {
        init()
    }

    fun submitLabel(imageLabel: ImageLabel) {
        if (
            validateLabel(imageLabel, false, Confidence.MODERATE)
            || validateLabel(imageLabel, true, Confidence.HIGH)
        ) {
            if (!cache.contains(imageLabel.text)) {
                cache.add(imageLabel.text)
                currentObject = cache.toList().last()
            }
        }
    }

    private fun validateLabel(
        imageLabel: ImageLabel,
        verifyError: Boolean,
        thresholdConfidence: Float
    ): Boolean {
        val errorFilter =
            if (verifyError)
                Filter.errorFilter.contains(imageLabel.text)
            else
                Filter.errorFilter.contains(imageLabel.text).not()

        return errorFilter &&
                !Filter.ignoreFilter.contains(imageLabel.text) &&
                imageLabel.confidence > thresholdConfidence
    }

    private fun init() {
        viewModelScope.launch {
            repeat(100) {
                delay(5000L)
                _detectedObjects.value = DetectedState.Success(currentObject)
            }
        }
    }

    fun initTTS(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = tts.setLanguage(Locale.ENGLISH)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                    _detectedObjects.value =
                        DetectedState.Error("Sorry, the currently selected language is not supported")
            } else {
                _detectedObjects.value =
                    DetectedState.Error("We're sorry, our app is not supported on this device")
            }
        }
    }

    fun speakTTS(recognisedText: String) {
        val pitch: Float = 1f
        val speed: Float = 0.8f

        tts.setPitch(pitch)
        tts.setSpeechRate(speed)
        tts.speak(recognisedText, TextToSpeech.QUEUE_ADD, null)
    }

    fun stopTTS() {
        if (::tts.isInitialized)
            tts.stop()
    }

    fun shutdownTTS() {
        if (::tts.isInitialized)
            tts.shutdown()
    }
}