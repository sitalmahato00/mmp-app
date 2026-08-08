package com.example.mmp_app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.core.utils.NepaliDateUtils
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NepaliDatePickerDialog(
    initialBsDate: String, // "YYYY-MM-DD"
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val todayBs = NepaliDateUtils.getTodayBs()
    val initialDate = if (initialBsDate.isNotEmpty()) initialBsDate else todayBs
    val parts = initialDate.split("-")
    
    var year by remember { mutableIntStateOf(parts[0].toInt()) }
    var month by remember { mutableIntStateOf(parts[1].toInt()) }
    var selectedDay by remember { mutableIntStateOf(parts[2].toInt()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Date (BS)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Year and Month Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Year Nav
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (year > 2000) year-- }) {
                        Icon(Icons.Rounded.ChevronLeft, null)
                    }
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.Center
                    )
                    IconButton(onClick = { if (year < 2090) year++ }) {
                        Icon(Icons.Rounded.ChevronRight, null)
                    }
                }

                // Month Nav
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        if (month > 1) month-- 
                        else if (year > 2000) { year--; month = 12 }
                    }) {
                        Icon(Icons.Rounded.ChevronLeft, null)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
                        Text(
                            text = NepaliDateUtils.bsMonthNamesEn[month - 1],
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = NepaliDateUtils.bsMonthNames[month - 1],
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { 
                        if (month < 12) month++ 
                        else if (year < 2090) { year++; month = 1 }
                    }) {
                        Icon(Icons.Rounded.ChevronRight, null)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Days Grid
            val daysInMonth = NepaliDateUtils.getDaysInBsMonth(year, month)
            
            // Adjust selected day if it exceeds current month's max days
            LaunchedEffect(year, month) {
                if (selectedDay > daysInMonth) selectedDay = daysInMonth
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val startDayOfWeek = remember(year, month) {
                val adOfFirst = NepaliDateUtils.bsToAd(year, month, 1)
                val cal = Calendar.getInstance()
                val adParts = adOfFirst.split("-")
                cal.set(adParts[0].toInt(), adParts[1].toInt() - 1, adParts[2].toInt())
                cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(280.dp)
            ) {
                items(startDayOfWeek) {
                    Box(modifier = Modifier.aspectRatio(1f))
                }

                items(daysInMonth) { i ->
                    val day = i + 1
                    val dateString = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)
                    val isSelected = selectedDay == day && parts[0].toInt() == year && parts[1].toInt() == month
                    val isToday = todayBs == dateString

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(4.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primaryContainer
                                    else -> Color.Transparent
                                }
                            )
                            .clickable { selectedDay = day },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day.toString(),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val tParts = todayBs.split("-")
                        year = tParts[0].toInt()
                        month = tParts[1].toInt()
                        selectedDay = tParts[2].toInt()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Today")
                }
                
                Button(
                    onClick = {
                        val formattedDate = String.format(Locale.US, "%04d-%02d-%02d", year, month, selectedDay)
                        onDateSelected(formattedDate)
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}
