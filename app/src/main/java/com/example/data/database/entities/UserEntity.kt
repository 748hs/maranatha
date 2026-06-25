package com.example.data.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val phoneNumber: String, // Unique Botswana phone number format
    val username: String,
    val isBlocked: Boolean = false,
    val isLoggedIn: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
