package com.example.feishuqa.app.keyboard

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.feishuqa.data.repository.ChatRepository

class ChatInputViewModel(private val repository: ChatRepository) : ViewModel() {

    private val _pendingImageUri = MutableLiveData<Uri?>()
    val pendingImageUri: LiveData<Uri?> = _pendingImageUri

    fun selectImage(uri: Uri) { _pendingImageUri.value = uri }
    fun clearPendingImage() { _pendingImageUri.value = null }

    fun sendMessage(text: String) {
        val uri = _pendingImageUri.value
        if (text.isEmpty() && uri == null) return

        repository.sendMessage(text, uri)

        clearPendingImage()
    }
}