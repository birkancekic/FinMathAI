package com.example.finmathai.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.finmathai.data.local.dao.AssetDao
import com.example.finmathai.data.local.entity.AssetEntity

@Database(
    entities = [AssetEntity::class], // Buraya AssetEntity'i eklediğinden emin ol
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assetDao(): AssetDao
}