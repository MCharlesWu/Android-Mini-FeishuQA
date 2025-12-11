package com.example.feishuqa.common.utils

import android.content.Context
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import org.json.JSONObject

class BaiduAsrManager(context: Context, private val listener: AsrListener) : EventListener {

    private var asr: EventManager = EventManagerFactory.create(context, "asr")

    init {
        asr.registerListener(this)
    }

    interface AsrListener {
        fun onReady()
        fun onVolumeChanged(volumePercent: Float)
        fun onPartialResult(text: String)
        fun onFinalResult(text: String)
        fun onError(errorCode: Int, message: String)
        // 【新增】识别彻底结束的回调
        fun onFinish()
    }

    fun start() {
        val params = HashMap<String, Any>()
        params[SpeechConstant.ACCEPT_AUDIO_VOLUME] = true
        params[SpeechConstant.PID] = 1537 // 必须 1537 才有标点
        params[SpeechConstant.DISABLE_PUNCTUATION] = false

        // 1. 开启 DNN 模型 (智能静音检测)
        params[SpeechConstant.VAD] = SpeechConstant.VAD_DNN

        params[SpeechConstant.VAD_ENDPOINT_TIMEOUT] = 800

        val json = JSONObject(params).toString()
        asr.send(SpeechConstant.ASR_START, json, null, 0, 0)
    }

    fun stop() {
        // 发送停止，等待 Final 和 Finish
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0)
    }

    fun cancel() {
        // 发送取消，SDK 会立即停止并回调 onFinish
        asr.send(SpeechConstant.ASR_CANCEL, null, null, 0, 0)
    }

    fun destroy() {
        asr.unregisterListener(this)
    }

    override fun onEvent(name: String, params: String?, data: ByteArray?, offset: Int, length: Int) {
        if (params == null) return
        try {
            val json = JSONObject(params)
            when (name) {
                SpeechConstant.CALLBACK_EVENT_ASR_READY -> listener.onReady()
                SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL -> {
                    val resultType = json.optString("result_type")
                    val results = json.optJSONArray("results_recognition")
                    if (results != null && results.length() > 0) {
                        val text = results.getString(0)
                        if ("final_result" == resultType) {
                            listener.onFinalResult(text)
                        } else if ("partial_result" == resultType) {
                            listener.onPartialResult(text)
                        }
                    }
                }
                SpeechConstant.CALLBACK_EVENT_ASR_FINISH -> {
                    listener.onFinish()
                }
                SpeechConstant.CALLBACK_EVENT_ASR_VOLUME -> {
                    val percent = json.optInt("volume-percent")
                    listener.onVolumeChanged(percent.toFloat())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}