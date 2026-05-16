package com.example.finmathai.di

import android.content.Context
import androidx.room.Room
import com.example.finmathai.data.local.AppDatabase
import com.example.finmathai.data.local.dao.AssetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "finmath_assets_db" // Veritabanı dosyasının adı
        ).fallbackToDestructiveMigration() // Şema değişirse eskiyi silip yeniyi kurar (Geliştirme aşamasında hayat kurtarır)
            .build()
    }

    @Provides
    fun provideAssetDao(database: AppDatabase): AssetDao {
        return database.assetDao()
    }
}