package com.example.mmp_app.domain.model

data class UserProfile(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
    val avatarUrl: String? = null
)
