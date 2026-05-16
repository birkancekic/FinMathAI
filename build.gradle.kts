plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false

    // Hilt Plugin'i buraya ekliyoruz (Üst dosyada tanımlanmalı)
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // DİKKAT: libs.plugins.kotlin.compose satırını BURADAN SİLDİK.
    // Çünkü Kotlin 1.9.24 sürümünde bu plugin mevcut değil.
}