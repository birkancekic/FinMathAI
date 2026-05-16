package com.example.finmathai.presentation.chat

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val generativeModel: GenerativeModel
) : ViewModel() {

    // UI'ın dinleyeceği durum değişkenleri
    var chatResponse by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set

    /**
     * Kullanıcıdan gelen soruyu AI'ya iletir.
     * Finansal matematik profesörü kişiliği ile cevap döner.
     */
    fun askFinanceQuestion(userPrompt: String) {
        if (userPrompt.isBlank()) return

        // AI'ya finansal bir kişilik kazandırıyoruz
        val finalPrompt = "Sen uzman bir finansal matematik profesörüsün. " +
                "Soruları finansal terimlerle, net ve profesyonel bir şekilde cevapla: $userPrompt"

        viewModelScope.launch {
            isLoading = true
            chatResponse = "" // Yeni soru için önceki cevabı temizle
            try {
                // GenerativeModel üzerinden içeriği oluşturuyoruz
                val result = generativeModel.generateContent(finalPrompt)
                chatResponse = result.text ?: "AI şu an cevap üretemiyor."
            } catch (e: Exception) {
                // Hata durumunda kullanıcıya dostça bir mesaj gösteriyoruz
                chatResponse = "Bağlantı Hatası: ${e.localizedMessage ?: "Beklenmedik bir hata oluştu."}"
            } finally {
                isLoading = false
            }
        }
    }
}