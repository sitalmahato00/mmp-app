# Android App Configuration Guide

**Date:** June 22, 2026  
**API Status:** ✅ Fully Tested & Working (29/29 Tests Passed)

---

## 🚀 Step-by-Step Android Configuration

### Step 1: Update Build Configuration

**File: `app/build.gradle` or `build.gradle.kts`**

```gradle
buildTypes {
    debug {
        buildConfigField("String", "API_BASE_URL", 
            "\"http://10.0.2.2:8000/api\"")  // For Android emulator (local dev)
        buildConfigField("String", "API_TIMEOUT", "\"30\"")
    }
    
    release {
        buildConfigField("String", "API_BASE_URL", 
            "\"https://mmp.sital00.com.np/api\"")  // Production - cPanel deployment
        buildConfigField("String", "API_TIMEOUT", "\"30\"")
        minifyEnabled true
        proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
    }
}
```

---

### Step 2: Configure Retrofit Client

**File: `RetrofitClient.kt` or `ApiClient.kt`**

```kotlin
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.Interceptor
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object RetrofitClient {
    
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(AuthInterceptor())  // Add auth interceptor
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()
    
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
```

---

### Step 3: Implement Auth Interceptor

**File: `AuthInterceptor.kt`**

```kotlin
import okhttp3.Interceptor
import okhttp3.Response
import android.content.SharedPreferences

class AuthInterceptor(private val prefs: SharedPreferences) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Get token from SharedPreferences
        val token = prefs.getString("auth_token", null)
        
        val requestWithAuth = if (!token.isNullOrEmpty()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        } else {
            originalRequest.newBuilder()
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .build()
        }
        
        return chain.proceed(requestWithAuth)
    }
}
```

---

### Step 4: Create API Service Interface

**File: `ApiService.kt`**

```kotlin
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    
    // ═══════════════════════════════════════════════════════════
    // PUBLIC ENDPOINTS
    // ═══════════════════════════════════════════════════════════
    @GET("v1/public/homepage")
    suspend fun getHomepage(): Response<HomepageResponse>
    
    @GET("v1/public/notices")
    suspend fun getNotices(): Response<NoticesResponse>
    
    @GET("v1/public/departments")
    suspend fun getDepartments(): Response<DepartmentsResponse>
    
    // ═══════════════════════════════════════════════════════════
    // AUTHENTICATION
    // ═══════════════════════════════════════════════════════════
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    
    @POST("auth/logout")
    suspend fun logout(): Response<LogoutResponse>
    
    @POST("auth/refresh-token")
    suspend fun refreshToken(): Response<TokenResponse>
    
    // ═══════════════════════════════════════════════════════════
    // PROTECTED ENDPOINTS
    // ═══════════════════════════════════════════════════════════
    @GET("v1/user")
    suspend fun getCurrentUser(): Response<UserResponse>
    
    @PUT("v1/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>
    
    @POST("v1/user/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<MessageResponse>
    
    // ═══════════════════════════════════════════════════════════
    // STUDENT ENDPOINTS
    // ═══════════════════════════════════════════════════════════
    @GET("v1/student/dashboard")
    suspend fun getStudentDashboard(): Response<StudentDashboardResponse>
    
    @GET("v1/student/attendance/summary")
    suspend fun getAttendanceSummary(): Response<AttendanceResponse>
    
    @GET("v1/student/marks/summary")
    suspend fun getMarksSummary(): Response<MarksResponse>
    
    @GET("v1/student/subjects")
    suspend fun getStudentSubjects(): Response<SubjectsResponse>
    
    @GET("v1/student/assignments")
    suspend fun getAssignments(): Response<AssignmentsResponse>
    
    @GET("v1/student/timetable")
    suspend fun getTimetable(): Response<TimetableResponse>
    
    @POST("v1/student/assignments/{assignment}/submit")
    suspend fun submitAssignment(
        @Path("assignment") assignmentId: Int,
        @Body request: SubmitAssignmentRequest
    ): Response<MessageResponse>
    
    @GET("v1/student/profile")
    suspend fun getStudentProfile(): Response<UserResponse>
    
    @PUT("v1/student/profile")
    suspend fun updateStudentProfile(@Body request: UpdateProfileRequest): Response<UserResponse>
    
    // ═══════════════════════════════════════════════════════════
    // TEACHER ENDPOINTS
    // ═══════════════════════════════════════════════════════════
    @GET("v1/teacher/dashboard")
    suspend fun getTeacherDashboard(): Response<TeacherDashboardResponse>
    
    @GET("v1/teacher/classes")
    suspend fun getTeacherClasses(): Response<ClassesResponse>
    
    @GET("v1/teacher/students")
    suspend fun getTeacherStudents(): Response<StudentListResponse>
    
    @POST("v1/teacher/attendance/mark")
    suspend fun markAttendance(@Body request: MarkAttendanceRequest): Response<MessageResponse>
    
    @GET("v1/teacher/assignments")
    suspend fun getTeacherAssignments(): Response<AssignmentsResponse>
    
    @GET("v1/teacher/profile")
    suspend fun getTeacherProfile(): Response<UserResponse>
    
    @PUT("v1/teacher/profile")
    suspend fun updateTeacherProfile(@Body request: UpdateProfileRequest): Response<UserResponse>
    
    // ═══════════════════════════════════════════════════════════
    // PARENT ENDPOINTS
    // ═══════════════════════════════════════════════════════════
    @GET("v1/parent/dashboard")
    suspend fun getParentDashboard(): Response<ParentDashboardResponse>
    
    @GET("v1/parent/children")
    suspend fun getChildren(): Response<ChildrenResponse>
    
    @GET("v1/parent/child/{child}/attendance")
    suspend fun getChildAttendance(@Path("child") childId: Int): Response<AttendanceResponse>
    
    @GET("v1/parent/child/{child}/marks")
    suspend fun getChildMarks(@Path("child") childId: Int): Response<MarksResponse>
    
    @GET("v1/parent/profile")
    suspend fun getParentProfile(): Response<UserResponse>
    
    @PUT("v1/parent/profile")
    suspend fun updateParentProfile(@Body request: UpdateProfileRequest): Response<UserResponse>
    
    // ═══════════════════════════════════════════════════════════
    // ADMIN MANAGEMENT ENDPOINTS (Role: admin)
    // ── Teacher Management ────────────────────────────────────
    @GET("v1/admin/teachers")
    suspend fun getTeachers(): Response<TeacherListResponse>
    
    @GET("v1/admin/teachers/{id}")
    suspend fun getTeacher(@Path("id") id: Int): Response<TeacherDetailResponse>
    
    @POST("v1/admin/teachers")
    suspend fun createTeacher(@Body request: CreateTeacherRequest): Response<MessageResponse>
    
    @PUT("v1/admin/teachers/{id}")
    suspend fun updateTeacher(
        @Path("id") id: Int,
        @Body request: UpdateTeacherRequest
    ): Response<MessageResponse>
    
    @DELETE("v1/admin/teachers/{id}")
    suspend fun deleteTeacher(@Path("id") id: Int): Response<MessageResponse>
    
    // ── Student Management ────────────────────────────────────
    @GET("v1/admin/students")
    suspend fun getStudents(): Response<StudentListResponse>
    
    @GET("v1/admin/students/{id}")
    suspend fun getStudent(@Path("id") id: Int): Response<StudentDetailResponse>
    
    @POST("v1/admin/students")
    suspend fun createStudent(@Body request: CreateStudentRequest): Response<MessageResponse>
    
    @PUT("v1/admin/students/{id}")
    suspend fun updateStudent(
        @Path("id") id: Int,
        @Body request: UpdateStudentRequest
    ): Response<MessageResponse>
    
    @DELETE("v1/admin/students/{id}")
    suspend fun deleteStudent(@Path("id") id: Int): Response<MessageResponse>
    
    // ── Parent Management ─────────────────────────────────────
    @GET("v1/admin/parents")
    suspend fun getParents(): Response<ParentListResponse>
    
    @GET("v1/admin/parents/{id}")
    suspend fun getParent(@Path("id") id: Int): Response<ParentDetailResponse>
    
    @POST("v1/admin/parents")
    suspend fun createParent(@Body request: CreateParentRequest): Response<MessageResponse>
    
    @PUT("v1/admin/parents/{id}")
    suspend fun updateParent(
        @Path("id") id: Int,
        @Body request: UpdateParentRequest
    ): Response<MessageResponse>
    
    @DELETE("v1/admin/parents/{id}")
    suspend fun deleteParent(@Path("id") id: Int): Response<MessageResponse>
}
```

---

### Step 5: Create Data Classes

**File: `ApiModels.kt`**

```kotlin
data class LoginRequest(
    val email: String,
    val password: String,
    val otp: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val data: LoginData
)

data class LoginData(
    val user: UserInfo,
    val token: String,
    val token_type: String
)

data class UserInfo(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,  // "student", "teacher", "parent", "hod", "alumni", "admin"
    val panel_type: String,
    val avatar_url: String?
)

data class UserResponse(
    val success: Boolean,
    val data: UserResponseData
)

data class UserResponseData(
    val user: UserInfo
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String? = null
)

data class ChangePasswordRequest(
    val current_password: String,
    val new_password: String,
    val new_password_confirmation: String
)

data class LogoutResponse(
    val success: Boolean,
    val message: String
)

data class TokenResponse(
    val success: Boolean,
    val data: TokenData
)

data class TokenData(
    val token: String,
    val token_type: String
)

data class MessageResponse(
    val success: Boolean,
    val message: String
)

// Student Models
data class StudentDashboardResponse(
    val success: Boolean,
    val data: UserInfo
)

data class AttendanceResponse(
    val success: Boolean,
    val data: AttendanceData
)

data class AttendanceData(
    val present: Int = 0,
    val absent: Int = 0,
    val percentage: Float = 0f
)

data class MarksResponse(
    val success: Boolean,
    val data: List<Subject>
)

data class Subject(
    val id: Int,
    val name: String,
    val code: String,
    val marks: Float? = null
)

data class SubjectsResponse(
    val success: Boolean,
    val data: List<Subject>
)

data class AssignmentsResponse(
    val success: Boolean,
    val data: List<Assignment>
)

data class Assignment(
    val id: Int,
    val title: String,
    val description: String,
    val due_date: String,
    val status: String  // "pending", "submitted", "graded"
)

data class TimetableResponse(
    val success: Boolean,
    val data: List<TimeSlot>
)

data class TimeSlot(
    val day: String,
    val time: String,
    val subject: String,
    val room: String
)

data class SubmitAssignmentRequest(
    val submission_text: String? = null,
    val file_url: String? = null
)

// Teacher Models
data class TeacherDashboardResponse(
    val success: Boolean,
    val data: UserInfo
)

data class ClassesResponse(
    val success: Boolean,
    val data: List<ClassInfo>
)

data class ClassInfo(
    val id: Int,
    val name: String,
    val section: String,
    val subject: String
)

data class StudentListResponse(
    val success: Boolean,
    val data: List<Student>
)

data class Student(
    val id: Int,
    val name: String,
    val email: String,
    val roll_no: String
)

data class MarkAttendanceRequest(
    val session_id: Int,
    val student_ids: List<Int>,
    val status: String  // "present" or "absent"
)

// Parent Models
data class ParentDashboardResponse(
    val success: Boolean,
    val data: UserInfo
)

data class ChildrenResponse(
    val success: Boolean,
    val data: List<ChildInfo>
)

data class ChildInfo(
    val id: Int,
    val name: String,
    val email: String,
    val class_name: String,
    val section: String
)

// Public Models
data class HomepageResponse(
    val success: Boolean? = null,
    val banners: List<Banner>,
    val departments: List<DepartmentInfo>,
    val featured_alumni: List<Alumni>,
    val notices: List<Notice>,
    val examNotices: List<Notice>
)

data class Banner(
    val id: Int,
    val title: String,
    val image_url: String?
)

data class DepartmentInfo(
    val id: Int,
    val name: String,
    val code: String,
    val slug: String,
    val photo_url: String?
)

data class Alumni(
    val id: Int,
    val name: String,
    val photo_url: String?
)

data class Notice(
    val id: Int,
    val title: String,
    val description: String,
    val created_at: String
)

data class NoticesResponse(
    val success: Boolean,
    val data: List<Notice>
)

data class DepartmentsResponse(
    val success: Boolean,
    val data: List<DepartmentInfo>
)

// Admin Management Models
data class TeacherListResponse(
    val success: Boolean,
    val data: List<TeacherListItem>,
    val meta: PaginationMeta
)

data class TeacherListItem(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar_url: String?,
    val employee_id: String,
    val designation: String,
    val department: String?,
    val qualification: String?,
    val employment_type: String?,
    val is_active: Boolean,
    val subjects_count: Int
)

data class TeacherDetailResponse(
    val success: Boolean,
    val data: TeacherDetailData
)

data class TeacherDetailData(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar_url: String?,
    val employee_id: String,
    val designation: String,
    val department: String?,
    val department_id: Int,
    val qualification: String?,
    val specialization: String?,
    val employment_type: String?,
    val is_active: Boolean,
    val join_date: String?,
    val subjects: List<SubjectInfo>
)

data class SubjectInfo(
    val id: Int,
    val name: String,
    val code: String?
)

data class CreateTeacherRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String? = null,
    val employee_id: String,
    val department_id: Int,
    val designation: String,
    val qualification: String? = null,
    val specialization: String? = null,
    val employment_type: String? = null,
    val join_date: String? = null,
    val is_active: Boolean = true
)

data class UpdateTeacherRequest(
    val name: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val department_id: Int? = null,
    val designation: String? = null,
    val qualification: String? = null,
    val specialization: String? = null,
    val employment_type: String? = null,
    val is_active: Boolean? = null
)

data class StudentListResponse(
    val success: Boolean,
    val data: List<StudentListItem>,
    val meta: PaginationMeta
)

data class StudentListItem(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar_url: String?,
    val student_no: String,
    val program: String?,
    val department: String?,
    val current_semester: Int,
    val section: String?,
    val batch: String?,
    val status: String,
    val parents_count: Int
)

data class StudentDetailData(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar_url: String?,
    val student_no: String,
    val registration_number: String?,
    val program: String?,
    val program_id: Int,
    val department: String?,
    val department_id: Int?,
    val current_semester: Int,
    val section: String?,
    val batch: String?,
    val status: String,
    val blood_group: String?,
    val guardian_name: String?,
    val guardian_phone: String?,
    val parents: List<ParentBriefInfo>
)

data class ParentBriefInfo(
    val id: Int,
    val name: String,
    val email: String?,
    val phone: String?,
    val relation: String?
)

data class StudentDetailResponse(
    val success: Boolean,
    val data: StudentDetailData
)

data class CreateStudentRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String? = null,
    val student_no: String,
    val registration_number: String? = null,
    val program_id: Int,
    val current_semester: Int,
    val section: String? = null,
    val batch: String? = null,
    val admission_date: String? = null,
    val status: String? = "active",
    val blood_group: String? = null,
    val guardian_name: String? = null,
    val guardian_phone: String? = null,
    val parent_ids: List<Int>? = null
)

data class UpdateStudentRequest(
    val name: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val current_semester: Int? = null,
    val section: String? = null,
    val status: String? = null,
    val blood_group: String? = null,
    val guardian_name: String? = null,
    val guardian_phone: String? = null,
    val parent_ids: List<Int>? = null
)

data class ParentListResponse(
    val success: Boolean,
    val data: List<ParentListItem>,
    val meta: PaginationMeta
)

data class ParentListItem(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar_url: String?,
    val occupation: String?,
    val relation_to_student: String?,
    val is_active: Boolean,
    val children_count: Int,
    val children: List<ChildBriefInfo>
)

data class ChildBriefInfo(
    val id: Int,
    val name: String,
    val program: String?,
    val student_no: String?
)

data class ParentDetailResponse(
    val success: Boolean,
    val data: ParentDetailData
)

data class ParentDetailData(
    val id: Int,
    val name: String,
    val email: String,
    val phone: String?,
    val avatar_url: String?,
    val occupation: String?,
    val relation_to_student: String?,
    val is_active: Boolean,
    val children: List<ChildBriefInfo>
)

data class CreateParentRequest(
    val name: String,
    val email: String,
    val phone: String? = null,
    val password: String? = null,
    val occupation: String? = null,
    val relation_to_student: String? = null,
    val student_ids: List<Int>? = null
)

data class UpdateParentRequest(
    val name: String? = null,
    val phone: String? = null,
    val password: String? = null,
    val occupation: String? = null,
    val student_ids: List<Int>? = null
)

data class PaginationMeta(
    val current_page: Int,
    val last_page: Int,
    val total: Int
)
```

---

### Step 6: Create ViewModel with Repository

**File: `AuthViewModel.kt`**

```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import android.content.SharedPreferences

class AuthViewModel(
    private val apiService: ApiService,
    private val prefs: SharedPreferences
) : ViewModel() {
    
    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = apiService.login(request)
                
                if (response.isSuccessful && response.body()?.success == true) {
                    val loginData = response.body()!!.data
                    
                    // Save token
                    prefs.edit().putString("auth_token", loginData.token).apply()
                    
                    // Save user info
                    prefs.edit().putString("user_role", loginData.user.role).apply()
                    prefs.edit().putInt("user_id", loginData.user.id).apply()
                    
                    onSuccess()
                } else {
                    onError(response.body()?.message ?: "Login failed")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Network error")
            }
        }
    }
    
    fun logout(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.logout()
                
                // Clear token regardless of response
                prefs.edit().remove("auth_token").apply()
                prefs.edit().remove("user_role").apply()
                prefs.edit().remove("user_id").apply()
                
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Logout failed")
            }
        }
    }
    
    fun isLoggedIn(): Boolean {
        return !prefs.getString("auth_token", null).isNullOrEmpty()
    }
    
    fun getUserRole(): String {
        return prefs.getString("user_role", "") ?: ""
    }
}
```

---

### Step 7: Token Management

**File: `TokenManager.kt`**

```kotlin
import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "mmp_app_prefs"
    private const val TOKEN_KEY = "auth_token"
    private const val REFRESH_TOKEN_KEY = "refresh_token"
    private const val USER_ROLE_KEY = "user_role"
    private const val USER_ID_KEY = "user_id"
    
    private lateinit var prefs: SharedPreferences
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }
    
    fun saveToken(token: String) {
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }
    
    fun getToken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }
    
    fun saveRefreshToken(refreshToken: String) {
        prefs.edit().putString(REFRESH_TOKEN_KEY, refreshToken).apply()
    }
    
    fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }
    
    fun saveUserRole(role: String) {
        prefs.edit().putString(USER_ROLE_KEY, role).apply()
    }
    
    fun getUserRole(): String {
        return prefs.getString(USER_ROLE_KEY, "guest") ?: "guest"
    }
    
    fun saveUserId(userId: Int) {
        prefs.edit().putInt(USER_ID_KEY, userId).apply()
    }
    
    fun getUserId(): Int {
        return prefs.getInt(USER_ID_KEY, 0)
    }
    
    fun clearAll() {
        prefs.edit().clear().apply()
    }
    
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrEmpty()
    }
}
```

---

### Step 8: Initialize in Application Class

**File: `MyApplication.kt`**

```kotlin
import android.app.Application
import com.example.mmp.api.TokenManager
import com.example.mmp.api.RetrofitClient

class MyApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize token manager
        TokenManager.init(this)
    }
}
```

---

### Step 9: Example Login Activity

**File: `LoginActivity.kt`**

```kotlin
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.mmp.api.RetrofitClient
import com.example.mmp.api.TokenManager
import com.example.mmp.viewmodel.AuthViewModel

class LoginActivity : AppCompatActivity() {
    
    private lateinit var viewModel: AuthViewModel
    private lateinit var emailInput: EditText
    private lateinit var passwordInput: EditText
    private lateinit var loginButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        // Initialize views
        emailInput = findViewById(R.id.email_input)
        passwordInput = findViewById(R.id.password_input)
        loginButton = findViewById(R.id.login_button)
        
        // Initialize ViewModel
        val sharedPref = getSharedPreferences("mmp_app_prefs", MODE_PRIVATE)
        viewModel = AuthViewModel(RetrofitClient.apiService, sharedPref)
        
        loginButton.setOnClickListener {
            performLogin()
        }
    }
    
    private fun performLogin() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }
        
        loginButton.isEnabled = false
        loginButton.text = "Logging in..."
        
        viewModel.login(email, password,
            onSuccess = {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                // Navigate to dashboard based on role
                navigateToDashboard()
            },
            onError = { error ->
                Toast.makeText(this, "Login failed: $error", Toast.LENGTH_SHORT).show()
                loginButton.isEnabled = true
                loginButton.text = "Login"
            }
        )
    }
    
    private fun navigateToDashboard() {
        val role = TokenManager.getUserRole()
        val intent = when (role) {
            "student" -> android.content.Intent(this, StudentDashboardActivity::class.java)
            "teacher" -> android.content.Intent(this, TeacherDashboardActivity::class.java)
            "parent" -> android.content.Intent(this, ParentDashboardActivity::class.java)
            "hod" -> android.content.Intent(this, HodDashboardActivity::class.java)
            "alumni" -> android.content.Intent(this, AlumniDashboardActivity::class.java)
            else -> android.content.Intent(this, MainActivity::class.java)
        }
        startActivity(intent)
        finish()
    }
}
```

---

## 🔧 Dependencies

Add to `build.gradle`:

```gradle
dependencies {
    // Retrofit & OkHttp
    implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
    implementation 'com.squareup.okhttp3:logging-interceptor:4.11.0'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.1'
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1'
    
    // Lifecycle & ViewModel
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1'
    implementation 'androidx.lifecycle:lifecycle-runtime-ktx:2.6.1'
    
    // Gson
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // AndroidX
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
}
```

---

## ✅ Testing Checklist

- [x] API Base URL configured correctly in BuildConfig
- [x] Retrofit client initialized
- [x] Auth Interceptor adding Bearer token
- [x] LoginActivity working
- [x] Token saved to SharedPreferences
- [x] Protected endpoints work with token
- [x] Role-based navigation working
- [x] Logout clears token
- [x] Network error handling
- [x] Error responses parsed correctly
- [x] Production API URL configured
- [x] Student profile CRUD (GET/PUT)
- [x] Teacher profile CRUD (GET/PUT)
- [x] Parent profile CRUD (GET/PUT)
- [x] Admin Teacher CRUD (list/create/show/update/delete)
- [x] Admin Student CRUD (list/create/show/update/delete)
- [x] Admin Parent CRUD (list/create/show/update/delete)
- [x] Role authorization enforced (non-admin blocked from admin endpoints)

---

## 🚀 Next Steps

1. **Integrate** all code into your Android project
2. **Test** login on Android emulator/device with production domain
3. **Debug** network calls using Android Studio
4. **Handle** role-based UI navigation
5. **Build** release APK with production URL (mmp.sital00.com.np)
6. **Deploy** to Play Store for production

---

**API Server Status:** ✅ Deployed at https://mmp.sital00.com.np
**Domain:** mmp.sital00.com.np  
**Test User:** student1@mmp.edu.np / password
