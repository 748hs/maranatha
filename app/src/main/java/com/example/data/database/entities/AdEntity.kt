package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "advertisements")
data class AdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val price: Double,
    val category: String,
    val location: String,
    val contactNumber: String,
    val sellerUsername: String,
    val imageUrlsJson: String, // Semi-colon ";" joined images or empty
    val createdAt: Long = System.currentTimeMillis()
)
