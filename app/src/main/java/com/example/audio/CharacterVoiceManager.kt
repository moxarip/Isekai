package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.CharacterVoiceType
import com.example.data.model.NpcDialogue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

class CharacterVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentSpeaker = MutableStateFlow<CharacterVoiceType?>(null)
    val currentSpeaker: StateFlow<CharacterVoiceType?> = _currentSpeaker.asStateFlow()

    companion object {
        private const val TAG = "CharacterVoiceManager"
    }

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
            tts = null
        }
    }

    override fun onInit(status: Int) {
        try {
            if (status == TextToSpeech.SUCCESS) {
                val arLocale = Locale.forLanguageTag("ar")
                val result = tts?.setLanguage(arLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.w(TAG, "Arabic TTS not fully available, falling back to default locale")
                    tts?.setLanguage(Locale.getDefault())
                }
                isInitialized = true

                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentSpeaker.value = null
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _currentSpeaker.value = null
                    }
                })
            } else {
                Log.w(TAG, "TTS Init non-success status: $status")
            }
        } catch (e: Throwable) {
            Log.e(TAG, "Error in onInit", e)
        }
    }

    fun speak(
        text: String,
        voiceType: CharacterVoiceType = CharacterVoiceType.NARRATOR,
        queueMode: Int = TextToSpeech.QUEUE_FLUSH,
        onComplete: (() -> Unit)? = null
    ) {
        if (!isInitialized || tts == null || text.isBlank()) return

        try {
            _currentSpeaker.value = voiceType
            tts?.setPitch(voiceType.pitch)
            tts?.setSpeechRate(voiceType.speedRate)

            val utteranceId = UUID.randomUUID().toString()
            val params = Bundle()
            tts?.speak(text, queueMode, params, utteranceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error speaking text", e)
        }
    }

    fun speakStorySequence(narrative: String, dialogue: NpcDialogue?) {
        if (!isInitialized || tts == null) return

        stop()

        // 1. Speak narrative with narrator voice
        if (narrative.isNotBlank()) {
            speak(
                text = narrative,
                voiceType = CharacterVoiceType.NARRATOR,
                queueMode = TextToSpeech.QUEUE_FLUSH
            )
        }

        // 2. Queue dialogue with character voice
        if (dialogue != null && dialogue.speechAr.isNotBlank()) {
            val characterType = CharacterVoiceType.fromSpeaker(dialogue.speakerName)
            speak(
                text = "${dialogue.speakerName} يقول: ${dialogue.speechAr}",
                voiceType = characterType,
                queueMode = TextToSpeech.QUEUE_ADD
            )
        }
    }

    fun stop() {
        try {
            tts?.stop()
            _isSpeaking.value = false
            _currentSpeaker.value = null
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
    }

    fun release() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing TTS", e)
        }
    }
}
