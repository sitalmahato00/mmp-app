package com.example.mmp_app.ui.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

sealed interface Routes : NavKey, Parcelable {
    @Serializable @Parcelize data object Login : Routes
    @Serializable @Parcelize data object OtpVerification : Routes
    @Serializable @Parcelize data object Dashboard : Routes
}
