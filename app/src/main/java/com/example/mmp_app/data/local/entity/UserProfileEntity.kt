package com.example.mmp_app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val avatarUrl: String? = null
)
