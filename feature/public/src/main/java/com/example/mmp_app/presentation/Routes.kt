package com.example.mmp_app.presentation

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
    @Serializable @Parcelize data object Notices : Routes
    @Serializable @Parcelize data object Routines : Routes
    @Serializable @Parcelize data object Exams : Routes
    @Serializable @Parcelize data object Results : Routes
    @Serializable @Parcelize data object Subjects : Routes
    @Serializable @Parcelize data class SubjectDetail(val subjectId: Int, val subjectName: String, val subjectCode: String? = null) : Routes
    @Serializable @Parcelize data object Timetable : Routes
    @Serializable @Parcelize data object Downloads : Routes
    @Serializable @Parcelize data object Profile : Routes
    @Serializable @Parcelize data object Settings : Routes
    @Serializable @Parcelize data object Notifications : Routes
    @Serializable @Parcelize data class RecordAttendance(val classId: Int, val subject: String) : Routes
    @Serializable @Parcelize data class RecordMarks(val classId: Int, val subject: String) : Routes
    @Serializable @Parcelize data class ChildDetails(val childId: Int, val name: String) : Routes
    @Serializable @Parcelize data class ChildAttendance(val childId: Int) : Routes
    @Serializable @Parcelize data class ChildAssignments(val childId: Int) : Routes
    @Serializable @Parcelize data class ChildResults(val childId: Int) : Routes
    @Serializable @Parcelize data class ChildTimetable(val childId: Int) : Routes
    @Serializable @Parcelize data object ChildrenList : Routes
    @Serializable @Parcelize data object CreateAssignment : Routes
    @Serializable @Parcelize data class EditAssignment(val assignmentId: Int) : Routes
    @Serializable @Parcelize data class AssignmentSubmissions(val assignmentId: Int) : Routes
}
