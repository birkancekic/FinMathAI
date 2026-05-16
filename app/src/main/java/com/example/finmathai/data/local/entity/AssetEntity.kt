package com.example.finmathai.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "assets") // Burayı "assets_table" yerine "assets" yaptık
data class AssetEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val assetName: String,
    val assetType: String,
    val apiSymbol: String,
    val amount: Double,
    val purchasePrice: Double,
    val location: String,
    val purchaseDate: Long = System.currentTimeMillis()
)