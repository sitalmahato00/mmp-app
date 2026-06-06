package com.example.mmp_app.ui.navigation

import android.os.Parcelable
import androidx.navigation3.runtime.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

sealed interface Routes : NavKey, Parcelable {
    @Serializable @Parcelize data object Splash : Routes
    @Serializable @Parcelize data object Login : Routes
    @Serializable @Parcelize data object OtpVerification : Routes
    @Serializable @Parcelize data object Dashboard : Routes
    @Serializable @Parcelize data object Attendance : Routes
    @Serializable @Parcelize data object Marks : Routes
    @Serializable @Parcelize data object Assignments : Routes
    @Serializable @Parcelize data object Fees : Routes
    @Serializable @Parcelize data object Notices : Routes
    @Serializable @Parcelize data class RecordAttendance(val classId: Int, val subject: String) : Routes
    @Serializable @Parcelize data class RecordMarks(val classId: Int, val subject: String) : Routes
    @Serializable @Parcelize data class ChildDetails(val childId: Int, val name: String) : Routes
}
