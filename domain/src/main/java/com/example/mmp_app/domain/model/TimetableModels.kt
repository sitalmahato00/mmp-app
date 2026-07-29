package com.example.mmp_app.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TimetableClass(
    @SerialName("id") val id: Int? = null,
    @SerialName("subject") val subject: String? = null,
    @SerialName("subject_code") val subjectCode: String? = null,
    @SerialName("teacher") val teacher: String? = null,
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("room") val room: String? = null,
    @SerialName("type") val type: String? = null,
    @SerialName("duration") val duration: Int? = null
)

@Serializable
data class DaySchedule(
    @SerialName("day") val day: String,
    @SerialName("classes") val classes: List<TimetableClass>
)

@Serializable
data class TimetableData(
    @SerialName("has_timetable") val hasTimetable: Boolean = false,
    @SerialName("semester") val semester: Int? = null,
    @SerialName("section") val section: String? = null,
    @SerialName("effective_from") val effectiveFrom: String? = null,
    @SerialName("academic_session") val academicSession: String? = null,
    @SerialName("timetable") val timetable: List<DaySchedule>? = emptyList()
)
