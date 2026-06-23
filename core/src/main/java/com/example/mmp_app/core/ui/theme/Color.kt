package com.example.mmp_app.core.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Blue Theme (Material 3) - Redesigned
val PrimaryBlue = Color(0xFF2563EB)
val SecondaryBlue = Color(0xFF60A5FA)
val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val AccentBlue = Color(0xFFDBEAFE)

val PrimaryLight = PrimaryBlue
val OnPrimaryLight = Color(0xFFFFFFFF)
val PrimaryContainerLight = AccentBlue
val OnPrimaryContainerLight = PrimaryBlue

val SecondaryLight = SecondaryBlue
val OnSecondaryLight = Color(0xFFFFFFFF)
val SecondaryContainerLight = AccentBlue.copy(alpha = 0.5f)
val OnSecondaryContainerLight = PrimaryBlue

val TertiaryLight = Color(0xFF3B82F6)
val OnTertiaryLight = Color(0xFFFFFFFF)

// Professional grayscale for Light Theme
val OnSurfaceLight = Color(0xFF1E293B)
val OnBackgroundLight = Color(0xFF1E293B)
val SurfaceVariantLight = Color(0xFFF1F5F9)
val OnSurfaceVariantLight = Color(0xFF64748B)
val OutlineLight = Color(0xFFE2E8F0)

// Welcome Card Gradient
val WelcomeGradientStart = Color(0xFF2563EB)
val WelcomeGradientEnd = Color(0xFF60A5FA)
val WelcomeGradientAccent = Color(0xFF3B82F6).copy(alpha = 0.8f)

// Dark Theme Palette (Keeping for now, though focus is Blue)
val PrimaryDark = Color(0xFF93C5FD)
val OnPrimaryDark = Color(0xFF1E3A8A)
val PrimaryContainerDark = Color(0xFF1E3A8A)
val OnPrimaryContainerDark = Color(0xFFDBEAFE)

val SecondaryDark = Color(0xFF60A5FA)
val OnSecondaryDark = Color(0xFF1E3A8A)
val SecondaryContainerDark = Color(0xFF1E3A8A)
val OnSecondaryContainerDark = Color(0xFFDBEAFE)

val SurfaceDark = Color(0xFF0F172A)
val OnSurfaceDark = Color(0xFFF1F5F9)
val BackgroundDark = Color(0xFF0F172A)
val OnBackgroundDark = Color(0xFFF1F5F9)
val SurfaceVariantDark = Color(0xFF1E293B)
val OnSurfaceVariantDark = Color(0xFF94A3B8)
val OutlineDark = Color(0xFF334155)

// Status Colors
val Error = Color(0xFFEF4444)
val Success = Color(0xFF10B981)
val Warning = Color(0xFFF59E0B)
val Info = Color(0xFF3B82F6)

// Old colors preserved for compatibility if needed (deprecated)
val NavyBluePrimary = PrimaryLight
val NavyBlueOnPrimary = OnPrimaryLight
val MaroonSecondary = SecondaryLight
val MaroonOnSecondary = OnSecondaryLight
